package fr.imacaron.kaamelott.gif.repository

import fr.imacaron.kaamelott.gif.NotFoundException
import fr.imacaron.kaamelott.gif.entity.Episode
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.varchar

class EpisodeRepository(
	private val db: Database,
	private val sceneRepository: SceneRepository
) {
	fun getSeasonEpisodes(season: SeasonEntity): Result<List<EpisodeEntity>> =
		Result.success(db.episodes.filter { it.season eq season.id }.map { it })

	fun getSeasonEpisodesCount(season: SeasonEntity): Result<Int> =
		Result.success(db.episodes.count { it.season eq season.id })

	fun getSeasonEpisode(season: SeasonEntity, number: Int): Result<Episode> =
		db.episodes.find { (it.season eq season.id) and (it.number eq number) }?.let {
			Result.success(Episode(sceneRepository, it, season))
		} ?: Result.failure(NotFoundException("Episode not found"))

	fun addEpisode(episode: EpisodeEntity): Result<EpisodeEntity> {
		db.episodes.add(episode)
		return Result.success(episode)
	}

}

object EpisodeTable: Table<EpisodeEntity>("EPISODES") {
	val id = int("id_episode").primaryKey().bindTo { it.id }
	val number = int("number").bindTo { it.number }
	val fps = int("fps").bindTo { it.fps }
	val width = int("width").bindTo { it.width }
	val height = int("height").bindTo { it.height }
	val title = varchar("title").bindTo { it.title }
	val duration = double("duration").bindTo { it.duration }
	val season = int("season").references(SeasonTable) { it.season }

	val seasons: SeasonTable get() = season.referenceTable as SeasonTable
}

interface EpisodeEntity: Entity<EpisodeEntity> {
	val id: Int
	var number: Int
	var fps: Int
	var width: Int
	var height: Int
	var title: String
	var duration: Double
	var season: SeasonEntity

	companion object: Entity.Factory<EpisodeEntity>()
}

val Database.episodes get() = this.sequenceOf(EpisodeTable)