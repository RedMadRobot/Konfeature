package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.exception.ConfigNameAlreadyExistException
import com.redmadrobot.konfeature.exception.KeyDuplicationException
import com.redmadrobot.konfeature.exception.NoFeatureConfigException
import com.redmadrobot.konfeature.exception.SourceNameAlreadyExistException
import com.redmadrobot.konfeature.helper.TestFeatureConfig
import com.redmadrobot.konfeature.helper.createEmptyFeatureConfig
import com.redmadrobot.konfeature.helper.createTestSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KonfeatureBuilderTest {

    @Test
    fun `when no any feature config registered - should throw exception`() {
        shouldThrow<NoFeatureConfigException> {
            KonfeatureBuilder().build()
        }
    }

    @Test
    fun `when config with duplicated keys added - should throw exception`() {
        val featureConfig = TestFeatureConfig(withDuplicates = true)

        val exception = shouldThrow<KeyDuplicationException> {
            KonfeatureBuilder().register(featureConfig).build()
        }

        exception.message shouldBe "values with keys <'a'> are duplicated in config '${featureConfig.name}'"
    }

    @Test
    fun `when source with same name added twice - should throw exception`() {
        val featureConfigName = "Test Feature Config"

        val sourceName = "Test Source"

        val exception = shouldThrow<SourceNameAlreadyExistException> {
            konfeature {
                addSource(createTestSource(sourceName))
                addSource(createTestSource(sourceName))
                register(createEmptyFeatureConfig(featureConfigName))
            }
        }

        exception.message shouldBe "source with name '$sourceName' already registered"
    }

    @Test
    fun `when feature config with same name registered twice - should throw exception`() {
        val featureConfigName = "Test Feature Config"

        val exception = shouldThrow<ConfigNameAlreadyExistException> {
            konfeature {
                register(createEmptyFeatureConfig(featureConfigName))
                register(createEmptyFeatureConfig(featureConfigName))
            }
        }

        exception.message shouldBe "feature config with name '$featureConfigName' already registered"
    }
}
