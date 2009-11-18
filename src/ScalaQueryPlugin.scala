import com.novocode.squery.session.{Database, Session}
import java.sql.SQLException
import play.db.DB
import play.db.jpa.QueryFunctions
import play.jobs._
import play.PlayPlugin
import play.test._
import java.lang.reflect.Method

import scala.util.DynamicVariable

package sq {
object Scala {
  val dyn = new DynamicVariable[Session](null)

  implicit def threadLocalSession: Session = {
    val s = dyn.value
    if (s eq null)
      throw new SQLException("No implicit session available; threadLocalSession can only be used within a withSession block")
    else s
  }
}

class ScalaQueryPlugin extends PlayPlugin {
  
  var db : Database = _
  override def onApplicationStart() {
    db = Database.forDataSource(DB.datasource)
  }
  
  override def beforeInvocation() {//(actionMethod : Method) {
    val s = db.createSession()
    Scala.dyn.value = s
  }

  override def afterInvocation() {
    if (Scala.dyn.value == null) {
      return
    } else {
      Scala.dyn.value.close()
      Scala.dyn.value = null
    }
  }
}
}