package object transaction {
  case class SessionHolder(session: Any)

  trait SessionAware[S] {
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit def unwrap(implicit holder: SessionHolder): S =
      holder.session.asInstanceOf[S]
  }

  trait TransactionAware {
    def tx[A](f: SessionHolder => A): A
    def txReadOnly[A](f: SessionHolder => A): A
  }
}
