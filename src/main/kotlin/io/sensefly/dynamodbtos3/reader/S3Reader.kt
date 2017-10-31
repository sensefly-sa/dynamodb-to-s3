package io.sensefly.dynamodbtos3.reader

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3Object
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URI
import javax.inject.Inject


class S3Reader(source: URI, amazonS3: AmazonS3) : Reader {

  private var s3Object: S3Object

  init {

    if (source.scheme != "s3") {
      throw IllegalArgumentException("File source must be an s3 URI (ie s3://my-bucket/folder/my-table.json)")
    }

    val bucket = source.host
    val key = source.path.substring(1)
    s3Object = amazonS3.getObject(bucket, key) ?: throw IllegalArgumentException("Cannot get S3 object $source")
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

  override fun get(source: URI): S3Reader {
    return S3Reader(source, amazonS3)
  }

}
