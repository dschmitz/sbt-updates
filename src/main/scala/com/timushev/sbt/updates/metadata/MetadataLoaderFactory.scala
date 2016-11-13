package com.timushev.sbt.updates.metadata

import java.net.URL

import com.timushev.sbt.updates.Downloader
import com.timushev.sbt.updates.versions.Version
import sbt._

import scala.collection.mutable
import scala.concurrent.Future

object MetadataLoaderFactory {

  val KnownProtocol = "(?i)^https?$".r

  def loader(logger: Logger, credentials: Seq[Credentials]): PartialFunction[Resolver, MetadataLoader] = Function.unlift[Resolver, MetadataLoader]({
    case repo: MavenRepository =>
      val downloader = new Downloader(credentials, logger)
      val url = new URL(repo.root)
      url.getProtocol match {
        case KnownProtocol() => Some(new MavenMetadataLoader(repo, downloader))
        case _ => None
      }
    case _ =>
      None
  }).andThen(cached)

  private val cache = mutable.Map[ModuleID, Future[Seq[Version]]]()

  private def cached(loader: MetadataLoader) = new CachingMetadataLoader(loader, cache)

}
