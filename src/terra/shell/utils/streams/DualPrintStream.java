package terra.shell.utils.streams;

import java.io.PrintStream;

public class DualPrintStream extends PrintStream{
	
	private PrintStream p1, p2;
	
	public DualPrintStream(PrintStream p1, PrintStream p2){
		super(p1);
		this.p1 = p1;
		this.p2 = p2;
		
	}
	@Override
	public void println(String s){
		p1.println(s);
		p2.println(s);
	}
	
	@Override 
	public void println(){
		p1.println();
		p2.println();
	}
	
	@Override
	public void print(String s){
		p1.print(s);
		p2.print(s);
	}
	
	

}
