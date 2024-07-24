package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.builder.konfeature
import com.redmadrobot.konfeature.helper.TestFeatureConfig
import com.redmadrobot.konfeature.helper.createTestSource
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor
import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KonfeatureTest {

    @Test
    fun `when correct config added - then code should pass`() {
        val featureConfig = TestFeatureConfig()

        konfeature {
            register(featureConfig)
        }
    }

    @Test
    fun `when correct config added - should be correct spec`() {
        // GIVEN
        val sourceNames = listOf("Test Source 1", "Test Source 2", "Test Source 3")
        val selectedSource = sourceNames[2]

        val featureConfig = TestFeatureConfig(
            cSourceSelectionStrategy = SourceSelectionStrategy.anyOf(selectedSource),
        )

        // WHEN
        val toggleEase = konfeature {
            register(featureConfig)
        }

        // THEN
        toggleEase.spec.size shouldBe 1

        val config = toggleEase.spec.first()

        config.name shouldBe featureConfig.name
        config.description shouldBe featureConfig.description
        config.values.size shouldBe 3

        assertSoftly(config) {
            values[0].apply {
                key shouldBe "a"
                description shouldBe "feature a desc"
                defaultValue shouldBe true
                sourceSelectionStrategy.select(sourceNames.toSet()).size shouldBe sourceNames.size
            }

            values[1].apply {
                key shouldBe "b"
                description shouldBe "feature b desc"
                defaultValue shouldBe true
                sourceSelectionStrategy.select(sourceNames.toSet()).size shouldBe 0
            }

            values[2].apply {
                key shouldBe "c"
                description shouldBe "feature c desc"
                defaultValue shouldBe "feature c"
                sourceSelectionStrategy.select(sourceNames.toSet()).first() shouldBe selectedSource
            }
        }
    }

    @Test
    fun `when source have value - config should return it`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("a" to false),
        )
        val featureConfig = TestFeatureConfig()

        konfeature {
            addSource(source)
            register(featureConfig)
        }

        // WHEN
        val a = featureConfig.a

        // THEN
        a shouldBe false
    }

    @Test
    fun `when source don't have value - config should return default value`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("b" to false),
        )
        val featureConfig = TestFeatureConfig()

        konfeature {
            addSource(source)
            register(featureConfig)
        }

        // WHEN
        val a = featureConfig.a

        // THEN
        a shouldBe true
    }

    @Test
    fun `when source have value with unexpected type - config should return default value`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("a" to 5),
        )
        val featureConfig = TestFeatureConfig()

        konfeature {
            addSource(source)
            register(featureConfig)
        }

        // WHEN
        val a = featureConfig.a

        // THEN
        a shouldBe true
    }

    @Test
    fun `when both sources contain same key - config should return value of first added source`() {
        // GIVEN
        val source1 = createTestSource(
            name = "Test source 1",
            values = mapOf("a" to false),
        )
        val source2 = createTestSource(
            name = "Test source 2",
            values = mapOf("a" to true),
        )

        val featureConfig = TestFeatureConfig()

        // WHEN
        konfeature {
            addSource(source1)
            addSource(source2)
            register(featureConfig)
        }

        val a = featureConfig.a

        // THEN
        a shouldBe false
    }

    @Test
    fun `when source specified by SourceSelectionStrategy - config should return value from it`() {
        // GIVEN
        val source1 = createTestSource(
            name = "Test source 1",
            values = mapOf("c" to "test_source_1_c"),
        )
        val source2 = createTestSource(
            name = "Test source 2",
            values = mapOf("c" to "test_source_2_c"),
        )

        val featureConfig = TestFeatureConfig(
            cSourceSelectionStrategy = SourceSelectionStrategy.anyOf(source2.name),
        )

        // WHEN
        konfeature {
            addSource(source1)
            addSource(source2)
            register(featureConfig)
        }

        val c = featureConfig.c

        // THEN
        c shouldBe "test_source_2_c"
    }

    @Test
    fun `when value changed by interceptor - config should return it`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf(
                "a" to false,
                "b" to true,
                "c" to "test_source_1_c",
            ),
        )

        val interceptedValue = "intercepted_value_c"

        val interceptor = object : Interceptor {
            override val name: String = "test interceptor"

            override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
                return if (key == "c") interceptedValue else null
            }
        }

        val featureConfig = TestFeatureConfig()

        // WHEN
        konfeature {
            addSource(source)
            addInterceptor(interceptor)
            register(featureConfig)
        }

        // THEN
        assertSoftly(featureConfig) {
            a shouldBe false
            b shouldBe true
            c shouldBe interceptedValue
        }
    }

    @Test
    fun `when value changed by interceptor but has unexpected type - config should return default`() {
        // GIVEN
        val interceptor = object : Interceptor {
            override val name: String = "test interceptor"

            override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
                return if (key == "c") 100 else null
            }
        }

        val featureConfig = TestFeatureConfig()

        // WHEN
        konfeature {
            addInterceptor(interceptor)
            register(featureConfig)
        }

        // THEN
        featureConfig.c shouldBe "feature c"
    }
}
