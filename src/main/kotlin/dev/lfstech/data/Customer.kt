package dev.lfstech.data

import org.jetbrains.exposed.dao.id.IntIdTable

object Customers : IntIdTable("customers") {
    val name = varchar("name", 100)
    val credit = long("credit")
}
