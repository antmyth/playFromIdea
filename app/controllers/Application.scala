package controllers

import play.api._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc._
import anorm._
import views.html.helper.form

object Application extends Controller {

  case class Registration(email: String, password: String, fullName: String)

  //What goes in the Form
  //- mapppin -> case classes
  //- single -> single form field
  //- tuples - > multiple form fields
  val registration = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "fullName" -> nonEmptyText
    )(Registration)(Registration.unapply)
  )

  def index = Action {
    Ok(views.html.index(registration))
  }

  def register = Action { implicit request =>
    registration.bindFromRequest().fold({
      erroredRegistrationForm => BadRequest(views.html.index(erroredRegistrationForm))
    },{
      form =>
        DB.withConnection{ implicit c =>
          SQL(
            """
              insert into User (email, password, fullname, isAdmin)
              values ({email}, {password}, {fullname}, {isAdmin})
            """).on(
              'email -> form.email,
              'password -> form.password,
              'fullname -> form.fullName,
              'isAdmin -> false
              ).executeInsert()
        }
        Ok
    })
  }

}