

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Lafran on 11/29/17.
 */

class SpoonObserver<T> {

    companion object {
        val debugable = false
    }

    private var observers = mutableListOf<T>()
    private var properties = mutableListOf<Property<*>>()

    fun register(observer: T) {
        if (observers.add(observer))
            initObserver(observer)
    }

    fun <V> observe(initialValue: V, onChanged: T.(old: V, new: V) -> Unit): ReadWriteProperty<Any?, V> {
        val property = Property<V>(initialValue, onChanged)
        properties.add(property)
        return property
    }

    fun remove(observer: T) {
        observers.remove(observer)
    }

    private fun initObserver(observer: T) {
        properties.forEach {
            it.initObserver(observer)
        }
    }

    private inner class Property<V>(initialValue: V, private val onChanged: T.(old: V, new: V) -> Unit) : ReadWriteProperty<Any?, V> {

        private var value = initialValue

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
            if(debugable){
                println("$thisRef, just call -> '${property.name}' ")
            }

            return value
        }

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
            val oldVal = this.value
            this.value = value
            observers.forEach {
                it.onChanged(oldVal, this.value)
            }

            if (debugable) {
                println("'${property.name} has been changed in $thisRef.' with value: $value")
            }
        }

        fun initObserver(observer: T) {
            observer.onChanged(value, value)
        }
    }
}

interface ObserverDelegate<V> {
    fun onValueChanged(old: V, new: V)
}