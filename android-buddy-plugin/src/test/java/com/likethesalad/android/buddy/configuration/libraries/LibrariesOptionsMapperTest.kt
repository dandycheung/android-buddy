package com.likethesalad.android.buddy.configuration.libraries

import com.google.common.truth.Truth.assertThat
import com.likethesalad.android.buddy.extension.libraries.LibrariesOptions
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.junit.Assert.fail
import org.junit.Test

@Suppress("UnstableApiUsage")
class LibrariesOptionsMapperTest {

    private val librariesOptionsMapper = LibrariesOptionsMapper()

    @Test
    fun `Map LibrariesOptions to LibrariesPolicy`() {
        assertThat(verifyMapping("UseAll")).isEqualTo(LibrariesPolicy.UseAll)
        assertThat(verifyMapping("IgnoreAll")).isEqualTo(LibrariesPolicy.IgnoreAll)
        assertThat(verifyMapping("UseOnly", listOf("abc", "def")))
            .isEqualTo(LibrariesPolicy.UseOnly(setOf("abc", "def")))
    }

    @Test
    fun `Expect exception when no valid library policy name is provided`() {
        try {
            verifyMapping("NotValidName")
            fail("Should've crashed above")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo(
                "Invalid library policy name: 'NotValidName', " +
                        "the available options are: [UseAll, IgnoreAll, UseOnly]"
            )
        }
    }

    @Test
    fun `Expect exception when no args are passed for UseOnly`() {
        try {
            verifyMapping("UseOnly")
            fail("Should've crashed above")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo(
                "No library ids specified for 'UseOnly', if you don't want to use any library you " +
                        "should set the libraries policy to 'IgnoreAll' instead."
            )
        }
    }

    @Test
    fun `Expect exception when args are passed for options that don't expect any args`() {
        verifyNoArgExpectedException("UseAll")
        verifyNoArgExpectedException("IgnoreAll")
    }

    private fun verifyNoArgExpectedException(policyName: String) {
        val dummyArgs = listOf("arg1", "arg2")
        try {
            verifyMapping(policyName, dummyArgs)
            fail("Should've crashed above")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("No args should be passed for the '$policyName' policy")
        }
    }

    private fun verifyMapping(policyName: String, args: List<Any> = emptyList()): LibrariesPolicy {
        val options = mockk<LibrariesOptions>()
        val policyNameProperty = createPropertyMock(policyName)
        val argsProperty = createListPropertyMock(args)
        every { options.policyName }.returns(policyNameProperty)
        every { options.args }.returns(argsProperty)

        return librariesOptionsMapper.librariesOptionsToLibrariesPolicy(options)
    }

    private fun <T> createPropertyMock(value: T): Property<T> {
        val property = mockk<Property<T>>()
        every { property.get() }.returns(value)
        return property
    }

    private fun <T> createListPropertyMock(value: List<T>): ListProperty<T> {
        val property = mockk<ListProperty<T>>()
        every { property.get() }.returns(value)
        return property
    }
}