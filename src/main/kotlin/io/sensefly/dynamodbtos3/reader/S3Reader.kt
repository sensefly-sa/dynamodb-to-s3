package io.sensefly.dynamodbtos3.reader

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3Object
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URL
import javax.inject.Inject


class S3Reader(source: URL, amazonS3: AmazonS3) : Reader {

  private val log = LoggerFactory.getLogger(javaClass)

  private var s3Object: S3Object

  init {

    log.debug("protocol: {}, host: {}, path: {}", source.protocol, source.host, source.path)

    if (source.protocol != "s3:" || source.path.substringAfterLast(".") != "json") {
      throw IllegalArgumentException("File source must be an s3 URI (ie s3://my-bucket/folder/my-table.json)")
    }

    val bucket = source.host
    val filePath = source.path

    s3Object = amazonS3.getObject(bucket, filePath) ?: throw IllegalArgumentException("Cannot get S3 object $source")
    log.debug("s3Object: {}", s3Object)
  }

  override fun read(): InputStream {
    return s3Object.objectContent
  }

  override fun close() {
    s3Object.close()
  }

}


@Component
class S3ReaderFactory @Inject constructor(private val amazonS3: AmazonS3) : ReaderFactory<S3Reader> {

  override fun get(source: URL): S3Reader {
    return S3Reader(source, amazonS3)
  }

}
