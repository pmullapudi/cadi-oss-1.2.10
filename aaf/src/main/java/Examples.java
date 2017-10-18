/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
import com.att.rosetta.env.RosettaEnv;

public class Examples {
	public static void main(String[] args) {
		if(args.length<1) {
			System.out.println("Usage: Examples <name> [\"optional\" - will show optional fields]");
		} else {
			boolean options = args.length>1&&"optional".equals(args[1]);
			try {
				RosettaEnv env = new RosettaEnv();
				System.out.println(com.att.aaf.client.Examples.print(env, args[0], options));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	

}
