package eu.freme.broker.tools;

/**
 * StarterHelper adds the command line parameter --spring.profiles.active=... to
 * a profile when such configuration does not exist in the command line
 * arguments yet. It is used to set the --spring.profile.active parameter in the
 * freme starter classes.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class StarterHelper {

	/**
	 * 
	 * @param args
	 *            Original command line arguments
	 * @param defaultProfile
	 *            Profile to add when no other profile is specified in the
	 *            arguments.
	 * @return
	 */
	public static String[] addProfile(String[] args, String defaultProfile) {

		for (String arg : args) {
			if (arg.toLowerCase().startsWith("--spring.profiles.active=")) {
				return args;
			}
		}

		String[] newArgs = new String[args.length + 1];
		for (int i = 0; i < args.length; i++) {
			newArgs[i] = args[i];
		}
		newArgs[args.length] = "--spring.profiles.active=" + defaultProfile;
		return newArgs;
	}
}
