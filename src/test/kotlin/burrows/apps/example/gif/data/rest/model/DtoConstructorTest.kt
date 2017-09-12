package burrows.apps.example.gif.data.rest.model

import com.google.common.truth.Truth.assert_
import com.squareup.moshi.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import org.reflections.scanners.FieldAnnotationsScanner
import org.reflections.scanners.MemberUsageScanner
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.MethodParameterNamesScanner
import org.reflections.scanners.MethodParameterScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.lang.reflect.Modifier

/**
 * @author [Jared Burrows](mailto:jaredsburrows@gmail.com)
 */
class DtoConstructorTest {
  private val MODEL_PACKAGE = "burrows.apps.example.gif.data.rest.model"
  private val DATA_TRANSPORT_OBJECT = "Dto"
  private lateinit var classes: Set<Class<*>>

  @BeforeEach
  fun setUp() {
    val reflections = Reflections(ConfigurationBuilder().filterInputsBy(
      FilterBuilder().includePackage(MODEL_PACKAGE))
      .setUrls(ClasspathHelper.forClassLoader())
      .setScanners(SubTypesScanner(false), TypeAnnotationsScanner(),
        FieldAnnotationsScanner(), MethodAnnotationsScanner(),
        MethodParameterScanner(), MethodParameterNamesScanner(),
        MemberUsageScanner()))
    classes = reflections.getSubTypesOf(Any::class.java)
  }

  @Test
  fun testMakeSureAllDtosHaveNoArgsPublicConstructors() {
    classes.forEach { clazz ->
      val className = clazz.name
      if (className.endsWith(DATA_TRANSPORT_OBJECT) && !hasNoArgConstructor(clazz)) {
        assert_().fail("Found no 'no-args' and public constructors in class $className")
      }
    }
  }

  @Test
  fun testMakeSureAllHaveNoFinalAnnotations() {
    // ASSERT
    classes.forEach { clazz ->
      val className = clazz.name
      if (className.endsWith(DATA_TRANSPORT_OBJECT) && hasNoFinalAnnotations(clazz)) {
        assert_().fail("Found finalized field with @SerializedNamed annotation in class $className")
      }
    }
  }

  /**
   * Make sure each DTO has a single no-args and public constructor:
   * public class Blah {
   *      public Blah() {
   *      }
   * }
   */
  private fun hasNoArgConstructor(klass: Class<*>): Boolean {
    return klass.declaredConstructors.any { field ->
      field.parameterTypes.isEmpty() && Modifier.isPublic(field.modifiers)
    }
  }

  /**
   * None of this:
   * @SerializedNamed("blah")
   * private final String blah;
   */
  private fun hasNoFinalAnnotations(klass: Class<*>): Boolean {
    return klass.declaredFields.any { field ->
      field.getAnnotation(Json::class.java) != null
        && Modifier.isFinal(field.modifiers)
    }
  }
}
