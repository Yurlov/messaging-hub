package сom.viktor.yurlov;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
		MvcRegisterTest.class,
		MvcLoginTest.class,
		MvcResetTest.class,
		MvcUpdateTest.class,
		MvcAccountTest.class,
		MvcAppTest.class,
		RoleCrudTest.class
})
public class TestSuite {
}