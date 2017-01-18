import java.util.ArrayList;
import java.util.Scanner;


class TopDown {
	Grammar grammar = new Grammar();
	ArrayList<FIRST> FirstTable = new ArrayList<FIRST>();
	ArrayList<FOLLOW> FollowTable = new ArrayList<FOLLOW>();
	Prod[][] ParseTable ;
	public static void main(String args[]){
		TopDown topDownParser = new TopDown();
		
		topDownParser.getInput();
		topDownParser.calculateFirst();
		topDownParser.calculateFollow();
		topDownParser.buildParseTable();
		topDownParser.displayTable();
		topDownParser.parseString();
	}
	public void getInput(){
		Scanner scan = new Scanner(System.in);
		String oneProd ;
		Prod tempProd ;
		while(!(oneProd=scan.nextLine()).equals(";")){
			tempProd = new Prod();
			String[] parts = oneProd.split("->");
			tempProd.var=parts[0];
			String[] splittedProd =parts[1].split("\\|");
			for( String elem :splittedProd){
				tempProd.prod.add(elem);
			}
			grammar.Production.add(tempProd);
		}
		grammar.StartSymbol = grammar.Production.get(0).var;
		for( Prod tempP : grammar.Production ){
			grammar.Variable.add(tempP.var);
		}
		for( Prod tempP : grammar.Production ){
			String s;
			for ( String pr :tempP.prod ){
				for( int i=0;i<pr.length();i++){
					s=String.valueOf(pr.charAt(i));
					if((grammar.Variable.contains(s)==false) && (s.equals("#")==false) && (grammar.Terminal.contains(s)==false) ){
						grammar.Terminal.add(String.valueOf(pr.charAt(i)));
					}
				}
			}
		}
	}
	public boolean isCalculated(String var,String check){
		if(check.equals("first")){
			for(FIRST elem:FirstTable){
				if(elem.item.equals(var))
					return true;
			}
		}
		if(check.equals("follow")){
			for(FOLLOW elem:FollowTable){
				if(elem.item.equals(var)){
					return true;
				}
			}
		}
		return false;
	}
	public ArrayList<String> find(String var,String check){
		
		if(check.equals("first")){
			for(FIRST elem:FirstTable){
				if(elem.item.equals(var))
					return elem.first;
			}
		}
		if(check.equals("follow")){
			for(FOLLOW elem:FollowTable){
				if(elem.item.equals(var))
					return elem.follow;
			}
		}
		return null;
	}
	public void calculateFollow(){
		for(String var: grammar.Variable){
			if(isCalculated(var,"follow") && (var.equals(grammar.Variable.get(0))==false)){
				continue;
			}
			computeFollow(var);
		}
		System.out.println("\n\n---FOLLOW TABLE---\nItem\tFollow");
		for(FOLLOW elem:FollowTable){
			System.out.print("\n"+elem.item+"\t"+elem.follow);
		}
		
	}
	public void calculateFirst(){
		FIRST tempFirst ;
		for(String term: grammar.Terminal){
			tempFirst = new FIRST();
			tempFirst.item = term;
			tempFirst.first.add(term);
			FirstTable.add(tempFirst);
		}
		for(String var: grammar.Variable){
			if(isCalculated(var,"first")){
				continue;
			}
			computeFirst(var);
		}
		System.out.println("\n\n---FIRST TABLE---\nVAR\tFirst");
		for(FIRST elem:FirstTable){
			System.out.print("\n"+elem.item+"\t"+elem.first);
		}
	}
	public void store(String var,ArrayList<String> tempList,String check){
		if(check.equals("first")){
			FIRST firstObject = new FIRST();
			firstObject.item=var;
			firstObject.first.addAll(tempList);
			FirstTable.add(firstObject);
		}
		if(check.equals("follow")){
			for(FOLLOW F:FollowTable){
				if(F.item.equals(var)){
					F.follow.addAll(tempList);
					return;
				}
			}
			FOLLOW followObject = new FOLLOW();
			followObject.item=var;
			followObject.follow.addAll(tempList);
			FollowTable.add(followObject);
		}
	}
	public ArrayList<String> computeFollow(String Elem){
		ArrayList<String> tempFollow = new ArrayList<String>();
		int loc,next;
		if(isCalculated(Elem,"follow")){
			return find(Elem,"follow");
		}
		for(Prod indvProd:grammar.Production){
			for(String miniprod:indvProd.prod){
				if((loc=miniprod.indexOf(Elem))!=-1){
					if(loc!=miniprod.length()-1){
						next = loc+1;
						tempFollow.addAll(find(String.valueOf(miniprod.charAt(next)),"first"));
						while(find(String.valueOf(miniprod.charAt(next)),"first").contains("#") && next!=miniprod.length()-1 ){
							tempFollow.remove("#");
							next=next+1;
							tempFollow.addAll(find(String.valueOf(miniprod.charAt(next)),"first"));
						}
						if(find(String.valueOf(miniprod.charAt(next)),"first").contains("#")){
							tempFollow.remove("#");
							tempFollow.addAll(computeFollow(indvProd.var));
						}
					}
					else{
						if(indvProd.var.equals(String.valueOf(miniprod.charAt(loc)))){
							continue;
						}
						tempFollow.addAll(computeFollow(indvProd.var));
						
					}
				}
			}
		}
		if(grammar.Variable.indexOf(Elem)==0){
			tempFollow.add("$");
		}
		for(int i=0;i<tempFollow.size()-1;i++){
			for(int j=i+1;j<tempFollow.size();j++){
				if(tempFollow.get(i).equals(tempFollow.get(j))){
					tempFollow.remove(i);
					i=i-1;
					break;
				}
			}
		}
		
		store(Elem,tempFollow,"follow");
		return tempFollow;
	}
	public ArrayList<String> computeFirst(String Elem){
		ArrayList<String> tempFirst = new ArrayList<String>();
		int next=1;
		if(isCalculated(Elem,"first")){
			return find(Elem,"first");
		}
		for(Prod indvProd: grammar.Production){
			if(indvProd.var.equals(Elem)){
				for(String miniprod:indvProd.prod){
					if(grammar.Variable.contains(String.valueOf(miniprod.charAt(0)))){
						tempFirst.addAll(computeFirst(String.valueOf(miniprod.charAt(0))));
						while(tempFirst.contains("#") && next!=miniprod.length()){
							tempFirst.remove("#");
							if(grammar.Variable.contains(String.valueOf(miniprod.charAt(next)))){
								tempFirst.addAll(computeFirst(String.valueOf(miniprod.charAt(next))));
								next=next+1;
							}
							else{
								tempFirst.addAll(find(String.valueOf(miniprod.charAt(next)),"first"));
								break;
							}
						}
					}
					else if(String.valueOf(miniprod.charAt(0)).equals("#")){
						tempFirst.add("#");
					}
					else{
						tempFirst.addAll(find((String.valueOf(miniprod.charAt(0))),"first"));
					}
				}
			}
		}
		for(int i=0;i<tempFirst.size()-1;i++){
			for(int j=i+1;j<tempFirst.size();j++){
				if(tempFirst.get(i).equals(tempFirst.get(j))){
					tempFirst.remove(i);
					i=i-1;
					break;
				}
			}
		}
		store(Elem,tempFirst,"first");
		return tempFirst;
	}
	
	public void buildParseTable(){
		ParseTable = new Prod[grammar.Variable.size()][grammar.Terminal.size()+1];
		ArrayList<String> tempFirst,tempFollow;
		tempFirst = new ArrayList<String>();
		tempFollow = new ArrayList<String>();
		int next;
		for(int i=0;i<grammar.Variable.size();i++){
			for(int j=0;j<grammar.Terminal.size()+1;j++){
				ParseTable[i][j] = new Prod();
				ParseTable[i][j].var = "-";
				ParseTable[i][j].prod.add("-");
			}
		}
		for(Prod indvProd: grammar.Production){
				for(String miniprod:indvProd.prod){
					if(String.valueOf(miniprod.charAt(0)).equals("#")){
						tempFirst.add("#");
					}
					else{
						tempFirst.addAll(find(String.valueOf(miniprod.charAt(0)),"first"));
					}
					next=1;
					while(tempFirst.contains("#") && (next!=miniprod.length())){
						tempFirst.remove("#");
						
						tempFirst.addAll(find(String.valueOf(miniprod.charAt(next)),"first"));
						next=next+1;
					}
					for(int i=0;i<tempFirst.size()-1;i++){
						for(int j=i+1;j<tempFirst.size();j++){
							if(tempFirst.get(i).equals(tempFirst.get(j))){
								tempFirst.remove(i);
								i=i-1;
								break;
							}
						}
					}
					for(String term:tempFirst){
						
						if(!term.equals("#")){
							ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].var = indvProd.var;
							if(ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.contains("-")){
								ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.clear();
								ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.add(miniprod);
							}else{
								ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.add(miniprod);
							}
						}
					}
					if(tempFirst.contains("#")){
						tempFollow.addAll(find(indvProd.var,"follow"));
						for(int i=0;i<tempFollow.size()-1;i++){
							for(int j=i+1;j<tempFollow.size();j++){
								if(tempFollow.get(i).equals(tempFollow.get(j))){
									tempFollow.remove(i);
									i=i-1;
									break;
								}
							}
						}
						for(String term:tempFollow){
							if(term.equals("$")){
								ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.size()].var = indvProd.var;
								if(ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.size()].prod.contains("-")){
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.size()].prod.clear();
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.size()].prod.add(miniprod);
								}else{
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.size()].prod.add("#");
								}
							}else{
								ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].var = indvProd.var;
								if(ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.contains("-")){
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.clear();
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.add("#");
								}else{
									ParseTable[grammar.Variable.indexOf(indvProd.var)][grammar.Terminal.indexOf(term)].prod.add("#");
								}
							}
						}
					}
					tempFirst.clear();
					tempFollow.clear();
				}
		}
	}
	
	public void displayTable(){
		System.out.println("\n\n---PARSE TABLE---");
		Prod cell = new Prod();
		String s ;
		for(String term:grammar.Terminal){
			System.out.format("%15s",term);
		}
		System.out.format("%15s\n","$");
		for(int i=0;i<grammar.Variable.size()+1;i++){
			System.out.format("%15s","----------------");
		}
		System.out.println("\n");
		for(int i=0;i<grammar.Variable.size();i++){
			s= grammar.Variable.get(i)+" |";
			System.out.format("%-15s",s);
			for(int j=0;j<grammar.Terminal.size()+1;j++){
				if(ParseTable[i][j].var.equals("-")){
					s = String.format("-");
				}
				else{
					s = String.format(ParseTable[i][j].var+"->"+ParseTable[i][j].prod);
				}
				System.out.format("%-15.10s",s);
			}
			System.out.println("\n");
		}
	}
	public void parseString(){
		Scanner scan = new Scanner(System.in);
		System.out.print("\nEnter String :");
		String input = scan.nextLine();
		String MATCHED, INPUT, ACTION;
		ArrayList<String> STACK = new ArrayList<String>();
		ArrayList<String> tempProd ;
		ArrayList<String> tempTerminal$ = new ArrayList<String>();
		tempTerminal$.addAll(grammar.Terminal);
		tempTerminal$.add("$");
		MATCHED ="";
		ACTION="";
		INPUT = input + "$";
		STACK.add("$");
		STACK.add(0,grammar.StartSymbol);
		System.out.println("\n\n\t\tMoves made by a predictive parser on input :"+input);
		System.out.println("\t\t--------------------------------------------------------------------------------------------------\n");
		System.out.format("%25s %25s %25s %25s","MATCHED","STACK","INPUT","ACTION");
		System.out.println();
		for(int i=0;i<4;i++){
			System.out.format("%25s ", "-----------");
		}
		System.out.format("\n%25s %25s %25s %25s",MATCHED, STACK, INPUT, ACTION);
		System.out.println();
		while(!STACK.get(0).equals("$")){
			if(STACK.get(0).equals(String.valueOf(INPUT.charAt(0)))){
				ACTION = "match "+String.valueOf(INPUT.charAt(0));
				MATCHED = MATCHED + String.valueOf(INPUT.charAt(0));
				STACK.remove(0);
				INPUT = INPUT.substring(1);
			}
			else if(grammar.Terminal.contains(STACK.get(0))){
				System.out.println("\n\n\t\t-------------------------------------ERROR----------------------------------------------------\n");
				System.out.println("\n\t\t---UNSUCCESSFUL---");
				return;
			}
			else if(ParseTable[grammar.Variable.indexOf(STACK.get(0))][tempTerminal$.indexOf(String.valueOf(INPUT.charAt(0)))].var.equals("-")){
				System.out.println("\n\n\t\t-------------------------------------ERROR----------------------------------------------------\n");
				System.out.println("\n\t\t---UNSUCCESSFUL---");
				return;
			}
			else{
				tempProd = new ArrayList<String>();
				tempProd.addAll(ParseTable[grammar.Variable.indexOf(STACK.get(0))][tempTerminal$.indexOf(String.valueOf(INPUT.charAt(0)))].prod);
				ACTION = "output "+STACK.get(0)+"->"+ParseTable[grammar.Variable.indexOf(STACK.get(0))][tempTerminal$.indexOf(String.valueOf(INPUT.charAt(0)))].prod;
				STACK.remove(0);
				for(int i=tempProd.get(0).length()-1;i>=0;i--){
					STACK.add(0,String.valueOf(tempProd.get(0).charAt(i)));
				}
			}
			if(STACK.get(0).equals("#")){
				STACK.remove(0);
			}
			System.out.format("%25s %25s %25s %25s",MATCHED, STACK, INPUT, ACTION);
			System.out.println();
		}
		if(String.valueOf(INPUT.charAt(0)).equals("$")){
			System.out.println("\n\t\t---SUCCESSFULL---");
		}
		else{
			System.out.println("\n\t\t---UNSUCCESSFUL---");
		}
		
	}
}
