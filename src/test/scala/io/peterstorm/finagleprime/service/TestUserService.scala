package io.peterstorm.finagleprime.service

import cats.effect.IO
import io.peterstorm.finagleprime.TestUsers.users
import io.peterstorm.finagleprime.model.UserName
import io.peterstorm.finagleprime.repository.algebra.UserRepository

object TestUserService {

  private val testUserRepo: UserRepository[IO] =
    (username: UserName) => IO {
      users.find(_.username.value == username.value)
    }

  val service: UserService[IO] = new UserService[IO](testUserRepo)

}
