package fr.imacaron.kaamelott.gif

import com.mchange.v2.c3p0.ComboPooledDataSource
import dev.kord.common.entity.Choice
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.value
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.addFile
import dev.kord.rest.request.KtorRequestException
import fr.imacaron.kaamelott.gif.repository.EpisodeRepository
import fr.imacaron.kaamelott.gif.repository.SceneRepository
import fr.imacaron.kaamelott.gif.repository.SeasonRepository
import fr.imacaron.kaamelott.gif.repository.SeriesRepository
import io.ktor.util.logging.*
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.io.path.Path

val logger = LoggerFactory.getLogger("fr.imacaron.kaamelott.gif.Main")

suspend fun main(args: Array<String>) {
    val token = System.getenv("TOKEN") ?: run {
        logger.error("No TOKEN specified")
        return
    }

    val cpds = ComboPooledDataSource().apply {
        driverClass = "org.mariadb.jdbc.Driver"
        jdbcUrl = System.getenv("DB_URL")
        user = System.getenv("DB_USER")
        password = System.getenv("DB_PASSWORD")
        minPoolSize = 5
        acquireIncrement = 5
        maxPoolSize = 10
    }

    val db = Database.connect(cpds)

    val sceneRepository = SceneRepository(db)
    val episodeRepository = EpisodeRepository(db, sceneRepository)
    val seasonRepository = SeasonRepository(db, episodeRepository)
    val seriesRepository = SeriesRepository(db, seasonRepository, episodeRepository)

    if(args.size > 1) {
        val loader = Loader(sceneRepository, episodeRepository, seasonRepository, seriesRepository)
        loader.loadSeries(args[0])
        loader.loadSeason(args[1].toInt())
        for(i in 1..loader.series.seasons.size) {
            val dir = File("${args[2]}$i")
            loader.loadEpisodesInSeason("Kaamelott\\.S[0-9]{2}E([0-9]+)\\.(.*)\\.mkv", i, dir)
        }
        return
    }

    val kaamelott = seriesRepository.getSeries("kaamelott").getOrElse {
        logger.error("Missing kaamelott")
        return
    }

    val kord = Kord(token)

    val episodeNumbers = kaamelott.seasons.associate {
        it.number to it.episodes.size
    }

    if(episodeNumbers.size != 6) {
        logger.error("Missing book")
        return
    }

    kord.createGlobalChatInputCommand("kaagif", "Une commande pour créer des gif kaamelot") {
        integer("livre", "Livre") {
            required = true
            for (i in episodeNumbers.keys) {
                choice("Livre $i", i.toLong())
            }
        }
        integer("episode", "Épisode") {
            required = true
            autocomplete = true
        }
        string("timecode", "Timecode sous la forme mm:ss") {
            required = true
        }
        string("text", "Texte") {
            required = true
        }
    }

    kord.on<AutoCompleteInteractionCreateEvent> {
        val options = interaction.command.data.options.value ?: return@on
        when(interaction.command.data.name.value) {
            "kaagif" -> {
                if(options[1].value.value?.focused.value == true) {
                    val ep = interaction.command.strings["episode"] ?: run {
                        logger.debug("No episode in command")
                        return@on
                    }
                    val choices = interaction.command.integers["livre"]?.let { livre ->
                        val number = episodeNumbers[livre.toInt()] ?: run {
                            logger.debug("Missing book in episodeNumbers")
                            return@on
                        }
                        (1..number).asSequence().map {
                            logger.debug("Choice(\"Épisode $it\", Optional(null), $it)")
                            Choice.IntegerChoice("Épisode $it", Optional(null), it.toLong())
                        }.filter {
                            (ep in it.value.toString()).apply {
                                logger.debug("${it.value} is keep with ep=$ep: $this")
                            }
                        }
                    } ?: (1..25).asSequence().map {
                        logger.debug("Choice(\"Épisode $it\", Optional(null), $it)")
                        Choice.IntegerChoice("Épisode $it", Optional(null), it.toLong())
                    }
                    interaction.suggest(choices.take(25).toList())
                }
            }
        }
    }

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        val user = interaction.user
        logger.debug("Receive command from ${user.effectiveName}. Defer it")
        val resp = interaction.deferPublicResponse()
        val name = UUID.randomUUID()
        try {
            val epNum = interaction.command.integers["episode"]?.toByte() ?: run {
                logger.debug("No ep num in command")
                resp.respondBadCommand(user)
                return@on
            }
            val book = interaction.command.integers["livre"]?.toByte() ?: run {
                logger.debug("No book in command")
                resp.respondBadCommand(user)
                return@on
            }
            val ep = Episode(epNum, book)
            val time = try {
                interaction.command.strings["timecode"]?.split(":")?.let {
                    if (it.size != 2) {
                        resp.respondTimecode(user)
                        return@on
                    }
                    it[0].toInt() * 60 + it[1].toInt()
                } ?: run {
                    logger.debug("No timecode in command")
                    resp.respondBadCommand(user)
                    return@on
                }
            } catch (e: NumberFormatException) {
                logger.debug("Time code not only numbers")
                resp.respondTimecode(user)
                return@on
            }
            if(time > ep.info.duration) {
                logger.debug("Timecode greater than episode duration")
                resp.respondTropLoin(ep.info.duration, time, user)
                return@on
            }
            if(time < 0) {
                logger.debug("Timecode less than 0")
                resp.respondTropCourt(user)
                return@on
            }
            val text = interaction.command.strings["text"] ?: run {
                logger.debug("No text in command")
                resp.respondBadCommand(user)
                return@on
            }
            val scene = (ep.info.sceneChange.indexOfFirst { it > time } - 1).coerceAtLeast(0) + 1
            logger.debug("Getting scene $scene, starting at ${ep.getSceneStart(scene)} and last ${ep.getSceneDuration(scene)}")
            logger.debug("Creating meme")
            ep.createMeme("$name", scene, text)
                .onFailure {
                    logger.debug("Creating meme failed", it)
                    when(it) {
                        is NotEnoughTimeException -> {
                            logger.debug("Not enough time to create scene")
                            resp.respondPortionTropCourte(user)
                        }
                        is ErrorWhileDrawingText -> {
                            logger.debug("Error while drawing text on scene")
                            resp.respondTexteErreur(user)
                        }
                    }
                }
                .onSuccess {
                    logger.debug("meme $it successfully created")
                    resp.respond {
                        content = "<@${user.id}>\n"
                        addFile(Path(it))
                    }
                }
        }catch (e: Exception) {
            when(e) {
                is KtorRequestException -> {
                    if(e.status.code == 413) {
                        val size = File("gif/$name.gif").length()
                        logger.warn("File $name.gif too large, ${size}B")
                        resp.repondTropGros(user, size, "$name.gif")
                    } else {
                        logger.error(e)
                        resp.respondUnknownError(user)
                    }
                }
                else -> {
                    logger.error(e)
                    resp.respondUnknownError(user)
                }
            }
        }
    }

    logger.info("Starting")
    kord.login()
    logger.info("Gracefully stopping")
}