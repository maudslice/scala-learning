import cats.Id

// pure操作: A => Id[A], 但Id[A]仅仅是A的别名, 所以直接返回
def pure[A](value: A): Id[A] = value

def map[A, B](initial: Id[A])(func: A => B): Id[B] = func(initial)

def flatMap[A, B](initial: Id[A])(func: A => Id[B]): Id[B] = func(initial)

val a = pure(123)
val b = map(a)(_ + 100)
flatMap(b)(_ + 100)