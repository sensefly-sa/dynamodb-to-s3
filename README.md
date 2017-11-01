# Backup DynamoDB to S3

[![Build Status](https://travis-ci.org/sensefly-sa/dynamodb-to-s3.svg?branch=master)](https://travis-ci.org/sensefly-sa/dynamodb-to-s3)
[![codecov](https://codecov.io/gh/sensefly-sa/dynamodb-to-s3/branch/master/graph/badge.svg)](https://codecov.io/gh/sensefly-sa/dynamodb-to-s3)


Simple DynamoDB dump to S3.
  
Read & write rates are limited by applying a ratio (`read-percentage`/`write-percentage`) to table's provisioned capacities.


## Usage

```
$ java -jar dynamodb-to-s3-<version>.jar --help

Usage: <main class> [options] [command] [command options]
  Options:
    --help

  Commands:
    backup      Backup DynamoDB tables to S3 bucket.
      Usage: backup [options]
        Options:
        * -t, --table
            Table to backup to S3. 
            Repeat this param to backup multiple tables.
            Default: []
        * -b, --bucket
            Destination S3 bucket.
            Default: <empty string>
          -r, --read-percentage
            Read capacity percentage.
            Default: 0.5
          -p, --pattern
            Destination file path pattern.
            Default: yyyy/MM/dd

    restore      Restore DynamoDB table from json file hosted on S3.
      Usage: restore [options]
        Options:
        * -t, --table
            Table to restore.
            Default: <empty string>
        * -s, --source
            Full S3 path to a JSON backup file 
            (s3://my-bucket/folder/my-table.json). 
          -w, --write-percentage
            Write capacity percentage.
            Default: 0.5
```

### Backup

```
java -jar dynamodb-to-s3-<version>.jar backup \
  --table my-table \
  --table my-other-table \
  --bucket my-bucket
```

### Restore

```
java -jar dynamodb-to-s3-<version>.jar restore \
  --table my-table \
  --source s3://my-bucket/2017/10/27/my-table.json
```

## AWS credentials

AWS credentials are read using [DefaultAWSCredentialsProviderChain](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html):
* Environment Variables: `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` and `AWS_REGION`
* Java System Properties: `aws.accessKeyId` and `aws.secretKey`
* Credential profiles file at the default location (`~/.aws/credentials`) shared by all AWS SDKs and the AWS CLI
* Credentials delivered through the Amazon EC2 container service if `AWS_CONTAINER_CREDENTIALS_RELATIVE_URI` environment 
variable is set and security manager has permission to access the variable
* Instance profile credentials delivered through the Amazon EC2 metadata service

## Running with Docker

```
docker run \
  -e AWS_ACCESS_KEY=... \
  -e AWS_SECRET_KEY=... \
  -e AWS_REGION=... \
  sensefly/dynamodb-to-s3 --help
```

## Build

Run `./mvnw package`