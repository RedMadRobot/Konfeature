package com.redmadrobot.konfeature.builder

import com.redmadrobot.konfeature.exception.GroupNameAlreadyExistException
import com.redmadrobot.konfeature.exception.KeyDuplicationException
import com.redmadrobot.konfeature.exception.NoFeatureGroupException
import com.redmadrobot.konfeature.exception.SourceNameAlreadyExistException
import com.redmadrobot.konfeature.helper.TestFeatureGroup
import com.redmadrobot.konfeature.helper.createEmptyFeatureGroup
import com.redmadrobot.konfeature.helper.createTestSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class KonfeatureBuilderTest {

    @Test
    fun `when no any feature group registered - should throw exception`() {
        shouldThrow<NoFeatureGroupException> {
            KonfeatureBuilder().build()
        }
    }

    @Test
    fun `when group with duplicated keys added - should throw exception`() {
        val featureGroup = TestFeatureGroup(withDuplicates = true)

        val exception = shouldThrow<KeyDuplicationException> {
            KonfeatureBuilder().register(featureGroup).build()
        }

        exception.message shouldBe "values with keys <'a'> are duplicated in group '${featureGroup.name}'"
    }

    @Test
    fun `when source with same name added twice - should throw exception`() {
        val featureGroupName = "Test Feature Group"

        val sourceName = "Test Source"

        val exception = shouldThrow<SourceNameAlreadyExistException> {
            konfeature {
                addSource(createTestSource(sourceName))
                addSource(createTestSource(sourceName))
                register(createEmptyFeatureGroup(featureGroupName))
            }
        }

        exception.message shouldBe "source with name '$sourceName' already registered"
    }

    @Test
    fun `when feature group with same name registered twice - should throw exception`() {
        val featureGroupName = "Test Feature Group"

        val exception = shouldThrow<GroupNameAlreadyExistException> {
            konfeature {
                register(createEmptyFeatureGroup(featureGroupName))
                register(createEmptyFeatureGroup(featureGroupName))
            }
        }

        exception.message shouldBe "feature group with name '$featureGroupName' already registered"
    }
}
