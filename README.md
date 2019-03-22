# Тесты к курсу «Технологии Java»

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)

## Домашнее задание 8. Параллельный запуск

* Напишите класс ParallelMapperImpl, реализующий интерфейс ParallelMapper.
```
public interface ParallelMapper extends AutoCloseable {
    <T, R> List<R> run(
        Function<? super T, ? extends R> f, 
        List<? extends T> args
    ) throws InterruptedException;

    @Override
    void close() throws InterruptedException;
}
```
 * Метод run должен параллельно вычислять функцию f на каждом из указанных аргументов (args).
 * Метод close должен останавливать все рабочие потоки.
 * Конструктор ParallelMapperImpl(int threads) создает threads рабочих потоков, которые могут быть использованы для распараллеливания.
 * К одному ParallelMapperImpl могут одновременно обращаться несколько клиентов.
 * Задания на исполнение должны накапливаться в очереди и обрабатываться в порядке поступления.
 * В реализации не должно быть активных ожиданий.
* Модифицируйте касс IterativeParallelism так, чтобы он мог использовать ParallelMapper.
 * Добавьте конструктор IterativeParallelism(ParallelMapper)
 * Методы класса должны делить работу на threads фрагментов и исполнять их при помощи ParallelMapper.
 * Должна быть возможность одновременного запуска и работы нескольких клиентов, использующих один ParallelMapper.
 * При наличии ParallelMapper сам IterativeParallelism новые потоки создавать не должен.

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.mapper scalar <ParallelMapperImpl>,<IterativeParallelism>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.mapper list <ParallelMapperImpl>,<IterativeParallelism>```

Внимание! Между полными именами классов `ParallelMapperImpl` и `IterativeParallelism`
должна быть запятая и не должно быть пробелов.

Исходный код тестов:

* [простой вариант](modules/info.kgeorgiy.java.advanced.mapper/info/kgeorgiy/java/advanced/mapper/ScalarMapperTest.java)
* [сложный вариант](modules/info.kgeorgiy.java.advanced.mapper/info/kgeorgiy/java/advanced/mapper/ListMapperTest.java)


## Домашнее задание 7. Итеративный параллелизм

* Реализуйте класс IterativeParallelism, который будет обрабатывать списки в несколько потоков.
* В простом варианте должны быть реализованы следующие методы:
  * minimum(threads, list, comparator) — первый минимум;
  * maximum(threads, list, comparator) — первый максимум;
  * all(threads, list, predicate) — проверка, что все элементы списка удовлетворяют предикату;
  * any(threads, list, predicate) — проверка, что существует элемент списка, удовлетворяющий предикату.
* В сложном варианте должны быть дополнительно реализованы следующие методы:
  * filter(threads, list, predicate) — вернуть список, содержащий элементы удовлетворяющие предикату;
  * map(threads, list, function) — вернуть список, содержащий результаты применения функции;
  * join(threads, list) — конкатенация строковых представлений элементов списка.
* Во все функции передается параметр threads — сколько потоков надо использовать при вычислении. Вы можете рассчитывать, что число потоков не велико.
* Не следует рассчитывать на то, что переданные компараторы, предикаты и функции работают быстро.
* При выполнении задания нельзя использовать Concurrency Utilities.
* Рекомендуется подумать, какое отношение к заданию имеют моноиды.

Тестирование

 * простой вариант:
   ```info.kgeorgiy.java.advanced.concurrent scalar <полное имя класса>```

   Класс должен реализовывать интерфейс
   [ScalarIP](modules/info.kgeorgiy.java.advanced.concurrent/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java).

 * сложный вариант:
   ```info.kgeorgiy.java.advanced.concurrent list <полное имя класса>```

   Класс должен реализовывать интерфейс
   [ListIP](modules/info.kgeorgiy.java.advanced.concurrent/info/kgeorgiy/java/advanced/concurrent/ListIP.java).

Исходный код тестов:

* [простой вариант](modules/info.kgeorgiy.java.advanced.concurrent/info/kgeorgiy/java/advanced/concurrent/ScalarIPTest.java)
* [сложный вариант](modules/info.kgeorgiy.java.advanced.concurrent/info/kgeorgiy/java/advanced/concurrent/ListIPTest.java)


## Домашнее задание 5 и 6. JarImplementor & Javadoc

* Документируйте класс Implementor и сопутствующие классы с применением Javadoc.
  * Должны быть документированы все классы и все члены классов, в том числе закрытые (private).
  * Документация должна генерироваться без предупреждений.
  * Сгенерированная документация должна содержать корректные ссылки на классы стандартной библиотеки.
* Для проверки, кроме исходного кода так же должны быть предъявлены:
  * скрипт для генерации документации;
  * сгенерированная документация.
* Данное домашнее задание сдается только вместе с предыдущим. Предыдущее домашнее задание отдельно сдать будет нельзя.


* Создайте .jar-файл, содержащий скомпилированный Implementor и сопутствующие классы.
  * Созданный .jar-файл должен запускаться командой java -jar.
  * Запускаемый .jar-файл должен принимать те же аргументы командной строки, что и класс Implementor.
  * Модифицируйте Implemetor так, что бы при запуске с аргументами -jar имя-класса файл.jar он генерировал .jar-файл с реализацией соответствующего класса (интерфейса).
* Для проверки, кроме исходного кода так же должны быть предъявлены:
  * скрипт для создания запускаемого .jar-файла, в том числе, исходный код манифеста;
  * запускаемый .jar-файл.
* Данное домашнее задание сдается только вместе с предыдущим. Предыдущее домашнее задание отдельно сдать будет нельзя.
Сложная версия. Решение должно быть модуляризовано.


Класс должен реализовывать интерфейс
[JarImpler](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/JarImpler.java).

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.implementor jar-interface <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.implementor jar-class <полное имя класса>```

Исходный код тестов:

* [простой вариант](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/InterfaceJarImplementorTest.java)
* [сложный вариант](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ClassJarImplementorTest.java)



## Домашнее задание 4. Implementor
* Реализуйте класс Implementor, который будет генерировать реализации классов и интерфейсов.
   * Аргументы командной строки: полное имя класса/интерфейса, для которого требуется сгенерировать реализацию.
   * В результате работы должен быть сгенерирован java-код класса с суффиксом Impl, расширяющий (реализующий) указанный класс (интерфейс).
   * Сгенерированный класс должен компилироваться без ошибок.
   * Сгенерированный класс не должен быть абстрактным.
   * Методы сгенерированного класса должны игнорировать свои аргументы и возвращать значения по умолчанию.
* В задании выделяются три уровня сложности:
   * Простой — Implementor должен уметь реализовывать только интерфейсы (но не классы). Поддержка generics не требуется.
   * Сложный — Implementor должен уметь реализовывать и классы и интерфейсы. Поддержка generics не требуется.
   * Бонусный — Implementor должен уметь реализовывать generic-классы и интерфейсы. Сгенерированный код должен иметь корректные параметры типов и не порождать UncheckedWarning.
   
Класс должен реализовывать интерфейс
[Impler](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java).

Тестирование:

 * простой вариант:
    ```info.kgeorgiy.java.advanced.implementor interface <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.implementor class <полное имя класса>```

Исходный код тестов:

* [простой вариант](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/InterfaceImplementorTest.java)
* [сложный вариант](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ClassImplementorTest.java)


## Домашнее задание 3. Студенты

Условие:
* Разработайте класс StudentDB, осуществляющий поиск по базе данных студентов.
    * Класс StudentDB должен реализовывать интерфейс StudentQuery (простая версия) или StudentGroupQuery (сложная версия).
    * Каждый методы должен состоять из ровного одного оператора. При этом длинные операторы надо разбивать на несколько строк.
* При выполнении задания следует обратить внимание на:
    * Применение лямбда-выражений и поток.
    * Избавление от повторяющегося кода.

Тестирование:

 * простой вариант:
    ```info.kgeorgiy.java.advanced.student StudentQuery <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.student StudentGroupQuery <полное имя класса>```

Исходный код:

 * простой вариант:
    [интерфейс](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentQuery.java),
    [тесты](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentQueryTest.java)
 * сложный вариант:
    [интерфейс](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentGroupQuery.java),
    [тесты](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentGroupQueryTest.java)

## Домашнее задание 2. ArraySortedSet

Условие:
* Разработайте класс ArraySet, реализующие неизменяемое упорядоченное множество.
* Класс ArraySet должен реализовывать интерфейс SortedSet (упрощенная версия) или NavigableSet (усложненная версия).
* Все операции над множествами должны производиться с максимально возможной асимптотической эффективностью.
При выполнении задания следует обратить внимание на:
* Применение стандартных коллекций.
* Избавление от повторяющегося кода.

Тестирование:

 * простой вариант:
    ```info.kgeorgiy.java.advanced.arrayset SortedSet <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.arrayset NavigableSet <полное имя класса>```

Исходный код тестов:

 * [простой вариант](modules/info.kgeorgiy.java.advanced.arrayset/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * [сложный вариант](modules/info.kgeorgiy.java.advanced.arrayset/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)


## Домашнее задание 1. Обход файлов

Условие:
* Разработайте класс Walk, осуществляющий подсчет хеш-сумм файлов.
  * Формат запуска
    * java Walk <входной файл> <выходной файл>
  * Входной файл содержит список файлов, которые требуется обойти.
  * Выходной файл должен содержать по одной строке для каждого файла. Формат строки:
    <шестнадцатеричная хеш-сумма> <путь к файлу>
  * Для подсчета хеш-суммы используйте алгоритм FNV.
  * Если при чтении файла возникают ошибки, укажите в качестве его хеш-суммы 00000000.
  * Кодировка входного и выходного файлов — UTF-8.
  * Размеры файлов могут превышать размер оперативной памяти.
  Пример

  Входной файл:

                        java/info/kgeorgiy/java/advanced/walk/samples/1
                        java/info/kgeorgiy/java/advanced/walk/samples/12
                        java/info/kgeorgiy/java/advanced/walk/samples/123
                        java/info/kgeorgiy/java/advanced/walk/samples/1234
                        java/info/kgeorgiy/java/advanced/walk/samples/1
                        java/info/kgeorgiy/java/advanced/walk/samples/binary
                        java/info/kgeorgiy/java/advanced/walk/samples/no-such-file
                    
  Выходной файл:

                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        2076af58 java/info/kgeorgiy/java/advanced/walk/samples/12
                        72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123
                        81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
                        00000000 java/info/kgeorgiy/java/advanced/walk/samples/no-such-file
                    
* Усложненная версия:
  * Разработайте класс RecursiveWalk, осуществляющий подсчет хеш-сумм файлов в директориях
  * Входной файл содержит список файлов и директорий, которые требуется обойти. Обход директорий осуществляется рекурсивно.
  Пример

  Входной файл:

                        java/info/kgeorgiy/java/advanced/walk/samples/binary
                        java/info/kgeorgiy/java/advanced/walk/samples
                    
  Выходной файл:

                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1
                        2076af58 java/info/kgeorgiy/java/advanced/walk/samples/12
                        72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123
                        81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary
                    
* При выполнении задания следует обратить внимание на:
  * Дизайн и обработку исключений, диагностику ошибок.
  * Программа должна корректно завершаться даже в случае ошибки.
  * Корректная работа с вводом-выводом.
  * Отсутствие утечки ресурсов.
  * Требования к оформлению задания.
  * Проверяется исходный код задания.
  * Весь код должен находиться в пакете ru.ifmo.rain.фамилия.walk.

Для того, чтобы протестировать программу:

 * Скачайте:
    * тесты
        * [info.kgeorgiy.java.advanced.base.jar](artifacts/info.kgeorgiy.java.advanced.base.jar)
        * [info.kgeorgiy.java.advanced.walk.jar](artifacts/info.kgeorgiy.java.advanced.walk.jar)
    * и библиотеки к ним:
        * [junit-4.11.jar](lib/junit-4.11.jar)
        * [hamcrest-core-1.3.jar](lib/hamcrest-core-1.3.jar)
 * Откомпилируйте решение домашнего задания
 * Протестируйте домашнее задание
    * Текущая директория должна:
       * содержать все скачанные `.jar` файлы;
       * содержать скомпилированное решение;
       * __не__ содержать скомпилированные самостоятельно тесты.
    * простой вариант:
        ```java -cp . -p . -m info.kgeorgiy.java.advanced.walk Walk <полное имя класса>```
    * сложный вариант:
        ```java -cp . -p . -m info.kgeorgiy.java.advanced.walk RecursiveWalk <полное имя класса>```

Исходный код тестов:

 * [простой вариант](modules/info.kgeorgiy.java.advanced.walk/info/kgeorgiy/java/advanced/walk/WalkTest.java)
 * [сложный вариант](modules/info.kgeorgiy.java.advanced.walk/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
