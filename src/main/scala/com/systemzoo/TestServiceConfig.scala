package com.systemzoo

import com.typesafe.config.ConfigFactory

case class TestServiceConfig(latency: Int, failRate: Double, badResponseRate: Double)
object TestServiceConfig {
  def apply(): TestServiceConfig = TestServiceConfig(latency, failRate, badResponseRate)

  private val defaultconfig = ConfigFactory.load()

  private lazy val latency          = defaultconfig.getInt("latency")
  private lazy val failRate         = defaultconfig.getDouble("fail-rate")
  private lazy val badResponseRate  = defaultconfig.getDouble("badresponse-rate")
}

