import dagger.Component
import assets.EmptyModule

@<error descr="@Component may only be applied to an interface or abstract class">Component</error>
class KClassComponent {

}

@<error descr="@Component may only be applied to an interface or abstract class">Component</error>(modules = [EmptyModule::class])
class KClassComponentWithModule {

}