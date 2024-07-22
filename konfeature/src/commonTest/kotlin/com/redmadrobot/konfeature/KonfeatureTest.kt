package com.redmadrobot.konfeature

import com.redmadrobot.konfeature.builder.konfeature
import com.redmadrobot.konfeature.helper.TestFeatureGroup
import com.redmadrobot.konfeature.helper.createTestSource
import com.redmadrobot.konfeature.source.FeatureValueSource
import com.redmadrobot.konfeature.source.Interceptor
import com.redmadrobot.konfeature.source.SourceSelectionStrategy
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KonfeatureTest {

    @Test
    fun `when correct group added - then code should pass`() {
        val featureGroup = TestFeatureGroup()

        konfeature {
            register(featureGroup)
        }
    }

    @Test
    fun `when correct group added - should be correct spec`() {
        // GIVEN
        val sourceNames = listOf("Test Source 1", "Test Source 2", "Test Source 3")
        val selectedSource = sourceNames[2]

        val featureGroup = TestFeatureGroup(
            cSourceSelectionStrategy = SourceSelectionStrategy.anyOf(selectedSource),
        )

        // WHEN
        val toggleEase = konfeature {
            register(featureGroup)
        }

        // THEN
        toggleEase.spec.size shouldBe 1

        val group = toggleEase.spec.first()

        group.name shouldBe featureGroup.name
        group.description shouldBe featureGroup.description
        group.values.size shouldBe 3

        assertSoftly(group) {
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
    fun `when source have value - group should return it`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("a" to false),
        )
        val featureGroup = TestFeatureGroup()

        konfeature {
            addSource(source)
            register(featureGroup)
        }

        // WHEN
        val a = featureGroup.a

        // THEN
        a shouldBe false
    }

    @Test
    fun `when source don't have value - group should return default value`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("b" to false),
        )
        val featureGroup = TestFeatureGroup()

        konfeature {
            addSource(source)
            register(featureGroup)
        }

        // WHEN
        val a = featureGroup.a

        // THEN
        a shouldBe true
    }

    @Test
    fun `when source have value with unexpected type - group should return default value`() {
        // GIVEN
        val source = createTestSource(
            name = "Test source",
            values = mapOf("a" to 5),
        )
        val featureGroup = TestFeatureGroup()

        konfeature {
            addSource(source)
            register(featureGroup)
        }

        // WHEN
        val a = featureGroup.a

        // THEN
        a shouldBe true
    }

    @Test
    fun `when both sources contain same key - group should return value of first added source`() {
        // GIVEN
        val source1 = createTestSource(
            name = "Test source 1",
            values = mapOf("a" to false),
        )
        val source2 = createTestSource(
            name = "Test source 2",
            values = mapOf("a" to true),
        )

        val featureGroup = TestFeatureGroup()

        // WHEN
        konfeature {
            addSource(source1)
            addSource(source2)
            register(featureGroup)
        }

        val a = featureGroup.a

        // THEN
        a shouldBe false
    }

    @Test
    fun `when source specified by SourceSelectionStrategy - group should return value from it`() {
        // GIVEN
        val source1 = createTestSource(
            name = "Test source 1",
            values = mapOf("c" to "test_source_1_c"),
        )
        val source2 = createTestSource(
            name = "Test source 2",
            values = mapOf("c" to "test_source_2_c"),
        )

        val featureGroup = TestFeatureGroup(
            cSourceSelectionStrategy = SourceSelectionStrategy.anyOf(source2.name),
        )

        // WHEN
        konfeature {
            addSource(source1)
            addSource(source2)
            register(featureGroup)
        }

        val c = featureGroup.c

        // THEN
        c shouldBe "test_source_2_c"
    }

    @Test
    fun `when value changed by interceptor - group should return it`() {
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

        val featureGroup = TestFeatureGroup()

        // WHEN
        konfeature {
            addSource(source)
            addInterceptor(interceptor)
            register(featureGroup)
        }

        // THEN
        assertSoftly(featureGroup) {
            a shouldBe false
            b shouldBe true
            c shouldBe interceptedValue
        }
    }

    @Test
    fun `when value changed by interceptor but has unexpected type - group should return default`() {
        // GIVEN
        val interceptor = object : Interceptor {
            override val name: String = "test interceptor"

            override fun intercept(valueSource: FeatureValueSource, key: String, value: Any): Any? {
                return if (key == "c") 100 else null
            }
        }

        val featureGroup = TestFeatureGroup()

        // WHEN
        konfeature {
            addInterceptor(interceptor)
            register(featureGroup)
        }

        // THEN
        featureGroup.c shouldBe "feature c"
    }
}
