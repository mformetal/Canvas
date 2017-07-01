package miles.scribble.dagger.viewmodel

import android.arch.lifecycle.ViewModel

import java.lang.annotation.Documented

import dagger.MapKey
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)