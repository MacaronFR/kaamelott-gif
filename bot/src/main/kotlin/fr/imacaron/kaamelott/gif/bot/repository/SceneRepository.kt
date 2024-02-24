package fr.imacaron.kaamelott.gif.repository

import fr.imacaron.kaamelott.gif.NotFoundException
import fr.imacaron.kaamelott.gif.entity.Scene
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.gte
import org.ktorm.dsl.lte
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int

class SceneRepository(
	private val db: Database
) {
	fun getEpisodeScenes(episode: EpisodeEntity): Result<List<SceneEntity>> {
		TODO("Not yet implemented")
	}

	fun getEpisodeScene(episode: EpisodeEntity, index: Int): Result<SceneEntity> {
		return db.scenes.find { (it.episode eq episode.id) and (it.index eq index) }?.let {
			Result.success(it)
		} ?: Result.failure(NotFoundException("Cannot found scene"))
	}

	fun getEpisodeSceneAt(episode: EpisodeEntity, at: Double): Result<Scene> {
		return db.scenes.find { (it.start lte at) and (it.end gte at) and (it.episode eq episode.id) }?.let {
			Result.success(Scene(it, episode))
		} ?: Result.failure(NotFoundException("Cannot find scene with this timecode"))
	}

	fun addEpisodeScene(info: SceneEntity): Result<SceneEntity> {
		db.scenes.add(info)
		return Result.success(info)
	}
}

object SceneTable: Table<SceneEntity>("SCENES") {
	val id = int("id_scene").primaryKey().bindTo { it.id }
	val start = double("start").bindTo { it.start }
	val end = double("end").bindTo { it.end }
	val index = int("index").bindTo { it.index }
	val episode = int("episode").references(EpisodeTable) { it.episode }

	val episodes: EpisodeTable get() = episode.referenceTable as EpisodeTable
}

interface SceneEntity: Entity<SceneEntity> {
	var id: Int
	var start: Double
	var end: Double
	var index: Int
	var episode: EpisodeEntity

	companion object: Entity.Factory<SceneEntity>()
}

internal val Database.scenes get() = this.sequenceOf(SceneTable)