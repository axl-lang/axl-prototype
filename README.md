Пример (сортировка пузырьком):

```axl
package axl.example

import java.lang.*
import java.util.*

fn bubbleSort(list: List) -> void {
    val n = list.size()
    var i = 0
    while (i < n - 1) {
        var j = 0
        while (j < n - i - 1) {
            val current = Integer((list.get(j) as Integer).intValue())
            val next = Integer((list.get(j + 1) as Integer).intValue())
            if (current.compareTo(next) > 0) {
                val temp = list.get(j)
                list.set(j, list.get(j + 1))
                list.set(j + 1, temp)
            }
            j = j + 1
        }
        i = i + 1
    }
}

fn main() -> void {
    val nums = ArrayList()

    nums.add(Integer(5))
    nums.add(Integer(6))
    nums.add(Integer(4))
    nums.add(Integer(43))
    nums.add(Integer(-3))
    nums.add(Integer(7))

    bubbleSort(nums)

    System.out.println(nums)
}
```
