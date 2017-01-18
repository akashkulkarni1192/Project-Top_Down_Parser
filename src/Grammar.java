import java.util.ArrayList;


public class Grammar {
	public ArrayList<String> Variable = new ArrayList<String>();
	public ArrayList<String> Terminal = new ArrayList<String>();
	public ArrayList<Prod> Production =new ArrayList<Prod>();
	public String StartSymbol;
}
class Prod{
	String var;
	ArrayList<String> prod = new ArrayList<String>();
}
class FIRST{
	String item;
	ArrayList<String> first = new ArrayList<String>();
}
class FOLLOW{
	String item;
	ArrayList<String> follow = new ArrayList<String>();
}





