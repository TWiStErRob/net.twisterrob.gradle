import java.security.SecureRandom;

class LintFailure {
	void f() {
		new SecureRandom().setSeed(0);
	}
}
