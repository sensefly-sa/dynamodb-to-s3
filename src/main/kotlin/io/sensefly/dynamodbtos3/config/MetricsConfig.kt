package io.sensefly.dynamodbtos3.config

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder
import com.codahale.metrics.MetricFilter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter
import io.github.azagniotov.metrics.reporter.cloudwatch.CloudWatchReporter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@Configuration
@EnableMetrics(proxyTargetClass = true)
class MetricsConfig : MetricsConfigurerAdapter() {

  private val log = LoggerFactory.getLogger(MetricsConfig::class.java)

  private val metricRegistry = MetricRegistry()
  private val healthCheckRegistry = HealthCheckRegistry()

  @Bean
  override fun getMetricRegistry(): MetricRegistry {
    return metricRegistry
  }

  @Bean
  override fun getHealthCheckRegistry(): HealthCheckRegistry {
    return healthCheckRegistry
  }

  fun setupJvmMetrics(jvmMetrics: Boolean) {
    if (jvmMetrics) {
      log.debug("Registering JVM gauges")
      metricRegistry.register(METRIC_JVM_MEMORY, MemoryUsageGaugeSet())
      metricRegistry.register(METRIC_JVM_GARBAGE, GarbageCollectorMetricSet())
      metricRegistry.register(METRIC_JVM_THREADS, ThreadStatesGaugeSet())
    }
  }

  fun setupCloudwatchMetrics(cloudwatchNamespace: String?) {
    if (cloudwatchNamespace == null) {
      log.info("Skip CloudWatch Metrics reporter")
    } else {
      log.info("Init CloudWatch Metrics reporter")
      CloudWatchReporter
          .forRegistry(metricRegistry, AmazonCloudWatchAsyncClientBuilder.defaultClient(), cloudwatchNamespace)
          .convertRatesTo(SECONDS)
          .convertDurationsTo(MILLISECONDS)
          .filter(MetricFilter.ALL)
          .build()
          .start(10, SECONDS)
    }
  }

  companion object {
    private const val METRIC_JVM_MEMORY = "jvm.memory"
    private const val METRIC_JVM_GARBAGE = "jvm.garbage"
    private const val METRIC_JVM_THREADS = "jvm.threads"
  }

}
