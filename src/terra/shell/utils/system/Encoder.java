package terra.shell.utils.system;

public final class Encoder {
	public static char parseByte(byte b) {
		switch (b) {
		case 0:
			return '0';
		case 1:
			return '1';
		case 2:
			return '2';
		case 3:
			return '3';
		case 4:
			return '4';
		case 5:
			return '5';
		case 6:
			return '6';
		case 7:
			return '7';
		case 8:
			return '8';
		case 9:
			return '9';
		case 10:
			return ':';
		// 11 NULL CHARACTER
		case 12:
			return '.';
		// 13-18 Unused Currently
		case 19:
			return '+';
		case 20:
			return '-';
		case 21:
			return 'A';
		case 22:
			return 'B';
		case 23:
			return 'C';
		case 24:
			return 'D';
		case 25:
			return 'E';
		case 26:
			return 'F';
		case 27:
			return 'G';
		case 28:
			return 'H';
		case 29:
			return 'I';
		case 30:
			return 'J';
		case 31:
			return 'K';
		case 32:
			return 'L';
		case 33:
			return 'M';
		case 34:
			return 'N';
		case 35:
			return 'O';
		case 36:
			return 'P';
		case 37:
			return 'Q';
		case 38:
			return 'R';
		case 39:
			return 'S';
		case 40:
			return 'T';
		case 41:
			return 'U';
		case 42:
			return 'V';
		case 43:
			return 'W';
		case 44:
			return 'X';
		case 45:
			return 'Y';
		case 46:
			return 'Z';
		case 57:
			return 'a';
		case 58:
			return 'b';
		case 59:
			return 'c';
		case 60:
			return 'd';
		case 61:
			return 'e';
		case 62:
			return 'f';
		case 63:
			return 'g';
		case 64:
			return 'h';
		case 65:
			return 'i';
		case 66:
			return 'j';
		case 67:
			return 'k';
		case 68:
			return 'l';
		case 69:
			return 'm';
		case 70:
			return 'n';
		case 71:
			return 'o';
		case 72:
			return 'p';
		case 73:
			return 'q';
		case 74:
			return 'r';
		case 75:
			return 's';
		case 76:
			return 't';
		case 77:
			return 'u';
		case 78:
			return 'v';
		case 79:
			return 'w';
		case 80:
			return 'x';
		case 81:
			return 'y';
		case 82:
			return 'z';
		}
		return '?';
	}

	public static byte parseChar(char b) {
		switch (b) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		case ':':
			return 10;
		case '.':
			return 12;
		case '+':
			return 19;
		case '-':
			return 20;
		case 'A':
			return 21;
		case 'B':
			return 22;
		case 'C':
			return 23;
		case 'D':
			return 24;
		case 'E':
			return 25;
		case 'F':
			return 26;
		case 'G':
			return 27;
		case 'H':
			return 28;
		case 'I':
			return 29;
		case 'J':
			return 30;
		case 'K':
			return 31;
		case 'L':
			return 32;
		case 'M':
			return 33;
		case 'N':
			return 34;
		case 'O':
			return 35;
		case 'P':
			return 36;
		case 'Q':
			return 37;
		case 'R':
			return 38;
		case 'S':
			return 39;
		case 'T':
			return 40;
		case 'U':
			return 41;
		case 'V':
			return 42;
		case 'W':
			return 43;
		case 'X':
			return 44;
		case 'Y':
			return 45;
		case 'Z':
			return 46;
		case 'a':
			return 57;
		case 'b':
			return 58;
		case 'c':
			return 59;
		case 'd':
			return 60;
		case 'e':
			return 61;
		case 'f':
			return 62;
		case 'g':
			return 63;
		case 'h':
			return 64;
		case 'i':
			return 65;
		case 'j':
			return 66;
		case 'k':
			return 67;
		case 'l':
			return 68;
		case 'm':
			return 69;
		case 'n':
			return 70;
		case 'o':
			return 71;
		case 'p':
			return 72;
		case 'q':
			return 73;
		case 'r':
			return 74;
		case 's':
			return 75;
		case 't':
			return 76;
		case 'u':
			return 77;
		case 'v':
			return 78;
		case 'w':
			return 79;
		case 'x':
			return 80;
		case 'y':
			return 81;
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
