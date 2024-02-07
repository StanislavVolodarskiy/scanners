# scanners

Никогда не используйте `InputStream` после того как надели на него `Scanner`.

Это демонстрация к [вопросу на ruSO "Scanner и System.in: нужно ли закрывать"](https://ru.stackoverflow.com/questions/1565206).

Хорошая практика требует закрыть сканнер. Он реализует интерфейсы `Closeable` и `AutoCloseable`. А это прямое указание что надо за собой убрать.

Но если сканнер закрывается, то он закрывает и поток, на который надет. Получается что нельзя дальше читать из `System.in`.

Нет проблем, обернем `System.in` в обёртку, которая имеет пустой метод `close()`. Сканнер его вызовет, мы ничего не закроем. Можно пользоваться `System.in` дальше. Хорошо? Нет.


`Scanner` кеширует данные из потока. Данные попавшие в кеш Scanner вам никто никогда не вернёт, они пропали. Если вы используете `Scanner` для чтения данных с клавиатуры, потери данных не будет. Кеш для клавиатурного ввода строчный, кажется что всё работает нормально. Ситуация испортится, если программа работает с перенаправленным файлом или конвейером.

`Producer.java` печатает последовательность целых чисел начиная с миллиарда. `Consumer.java` читает числа и проверяет что они последовательно растут. Если число на входе не совпадает с ожиданием, данные где-то теряются.


`Consumer.readLong()` читает одно длинное целое. Для этого создаётся сканнер, который, следуя хорошей практике, закрывается после использования:
```
    private static long readLong() {
        try (Scanner s = new Scanner(in)) {
            return s.nextLong();
        }
    }
```

Чтобы при закрытии сканера входной поток не закрывался используется обёртка с пустым `close()`:

```
    private static InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            return System.in.read();
        }

        @Override
        public void close() {
        }
    };
```

Запускаем:

```
$ javac Producer.java Consumer.java

$ java Producer | java Consumer 
1000000000 1000000000
1000000001 44
^C
```

Всё закончилось быстро. Откуда взялось число *44*? Вот откуда:

```
$ java Producer | head -c 8192 | tail
1000000735
1000000736
1000000737
1000000738
1000000739
1000000740
1000000741
1000000742
1000000743
10000007^C
```

Это распечатан конец восьмого килобайта входного потока. Он обрывается посреди числа *1000000744*. Последние две цифры не поместились. В программе первый сканнер считал *8192* байта, вернул из них одно число и закрылся, освободив буфер с данными. Второй сканер начал читать с последнего места и первые символы, которые он увидел, были `"44\n"`. Второй сканер вернул число *44*.

На вашей системе результат может быть другой, зависит от размера буфера который использует сканнер. Так или иначе данные теряются.

Вывод: закрывать или нет сканер не важно. Данные уже потеряны. Если вы отдали `InputStream` сканнеру, не читайте из этого потока другими способами. Сканнер им владеет, его состояние вам не известно.
