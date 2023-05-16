import cats.Eval

// 使用Eval将下面的foldRight实现为stack安全的版本
def foldRight[A, B](as: List[A], acc: B)(fn: (A, B) => B): B =
  as match {
    case head :: tail => fn(head, foldRight(tail, acc)(fn))
    case Nil => acc
  }

def foldRightEval[A, B](as: List[A], acc: Eval[B])(fn: (A, Eval[B]) => Eval[B]): Eval[B] =
  as match {
    case head:: tail => Eval.defer(fn(head, foldRightEval(tail, acc)(fn)))
    case Nil => acc
  }

def foldRightSafety[A, B](as: List[A], acc: B)(fn: (A, B) => B): B =
  foldRightEval(as, Eval.now(acc))((a, b) => {
    b.map(bv => fn(a, bv))
  }).value

foldRightSafety((1 to 100000).toList, 0L)(_ + _)
//foldRight((1 to 100000).toList, 0L)(_ + _) // java.lang.StackOverflowError