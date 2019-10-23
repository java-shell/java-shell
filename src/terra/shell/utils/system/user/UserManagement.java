package terra.shell.utils.system.user;

import java.io.File;
import java.util.ArrayList;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;

public final class UserManagement {
	private static ArrayList<User> registeredUsers = new ArrayList<User>(); // TODO change to hashtable<String, User>,
																			// so access by name is easier
	private static boolean init = false;

	public static void init() {
		if (init)
			return;

		Configuration conf = Launch.getConfig("userManagement");
		if (conf == null) {
			conf = new Configuration(new File(Launch.getConfD(), "userManagement"));
			conf.setValue("userNames", "");
		}
		String userNames = (String) conf.getValue("userNames");
		String[] users = userNames.split(",");
		for (String user : users) {
			// TODO register users based on name
		}
		init = true;
	}

	public static void registerUser() {

	}

	private static final class PasswordEncoder {
		public static char parseByte(byte b) {
			switch (b) {
			case 10:
				return '0';
			case 5:
				return '1';
			case 23:
				return '2';
			case 11:
				return '3';
			case 45:
				return '4';
			case 64:
				return '5';
			case 33:
				return '6';
			case 56:
				return '7';
			case 102:
				return '8';
			case 20:
				return '9';
			case 13:
				return ':';
			// 11 NULL CHARACTER
			case 14:
				return '.';
			// 13-18 Unused Currently
			case 127:
				return '+';
			case 120:
				return '-';
			case 4:
				return 'A';
			case 2:
				return 'B';
			case 1:
				return 'C';
			case 67:
				return 'D';
			case 87:
				return 'E';
			case 121:
				return 'F';
			case 118:
				return 'G';
			case 35:
				return 'H';
			case 89:
				return 'I';
			case 96:
				return 'J';
			case 39:
				return 'K';
			case 91:
				return 'L';
			case 47:
				return 'M';
			case 50:
				return 'N';
			case 7:
				return 'O';
			case 79:
				return 'P';
			case 80:
				return 'Q';
			case 3:
				return 'R';
			case 61:
				return 'S';
			case 52:
				return 'T';
			case 88:
				return 'U';
			case 76:
				return 'V';
			case 71:
				return 'W';
			case 38:
				return 'X';
			case 83:
				return 'Y';
			case 94:
				return 'Z';
			case 62:
				return 'a';
			case 75:
				return 'b';
			case 115:
				return 'c';
			case 119:
				return 'd';
			case 122:
				return 'e';
			case 116:
				return 'f';
			case 101:
				return 'g';
			case 104:
				return 'h';
			case 85:
				return 'i';
			case 66:
				return 'j';
			case 63:
				return 'k';
			case 68:
				return 'l';
			case 72:
				return 'm';
			case 90:
				return 'n';
			case 81:
				return 'o';
			case 77:
				return 'p';
			case 99:
				return 'q';
			case 55:
				return 'r';
			case 16:
				return 's';
			case 97:
				return 't';
			case 8:
				return 'u';
			case 9:
				return 'v';
			case 117:
				return 'w';
			case 126:
				return 'x';
			case 74:
				return 'y';
			case 82:
				return 'z';
			}
			return '?';
		}

		public static byte parseChar(char b) {
			// TODO Swap return with value
			switch (b) {
			case '0':
				return 10;
			case '1':
				return 5;
			case '2':
				return 23;
			case '3':
				return 11;
			case '4':
				return 45;
			case '5':
				return 65;
			case '6':
				return 33;
			case '7':
				return 56;
			case '8':
				return 102;
			case '9':
				return 20;
			case ':':
				return 13;
			case '.':
				return 14;
			case '+':
				return 127;
			case '-':
				return 120;
			case 'A':
				return 4;
			case 'B':
				return 2;
			case 'C':
				return 1;
			case 'D':
				return 67;
			case 'E':
				return 87;
			case 'F':
				return 121;
			case 'G':
				return 118;
			case 'H':
				return 35;
			case 'I':
				return 89;
			case 'J':
				return 96;
			case 'K':
				return 39;
			case 'L':
				return 91;
			case 'M':
				return 47;
			case 'N':
				return 50;
			case 'O':
				return 7;
			case 'P':
				return 79;
			case 'Q':
				return 80;
			case 'R':
				return 3;
			case 'S':
				return 61;
			case 'T':
				return 52;
			case 'U':
				return 88;
			case 'V':
				return 76;
			case 'W':
				return 71;
			case 'X':
				return 38;
			case 'Y':
				return 83;
			case 'Z':
				return 94;
			case 'a':
				return 62;
			case 'b':
				return 75;
			case 'c':
				return 115;
			case 'd':
				return 119;
			case 'e':
				return 122;
			case 'f':
				return 116;
			case 'g':
				return 101;
			case 'h':
				return 104;
			case 'i':
				return 85;
			case 'j':
				return 66;
			case 'k':
				return 63;
			case 'l':
				return 68;
			case 'm':
				return 72;
			case 'n':
				return 90;
			case 'o':
				return 81;
			case 'p':
				return 77;
			case 'q':
				return 99;
			case 'r':
				return 55;
			case 's':
				return 16;
			case 't':
				return 97;
			case 'u':
				return 8;
			case 'v':
				return 9;
			case 'w':
				return 117;
			case 'x':
				return 126;
			case 'y':
				return 74;
			case 'z':
				return 82;

			}
			return 11;

		}

		public static byte[] parseString(String s) {
			char[] c = s.toCharArray();
			byte[] d = new byte[c.length];
			for (int i = 0; i < c.length; i++) {
				d[i] = parseChar(c[i]);
			}
			return d;
		}

		public static String parseByteArray(byte[] b) {
			char[] c = new char[b.length];
			for (int i = 0; i < b.length; i++) {
				c[i] = parseByte(b[i]);
			}
			return new String(c);
		}

	}

}
