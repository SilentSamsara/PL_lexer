package parser.models;

import java.util.*;

import parser.tools.SRException;

public class SLRAnalyserTable {
	public static List<List<Lattice>> ACTION = new ArrayList<List<Lattice>>();
	public static List<List<Lattice>> GOTO = new ArrayList<List<Lattice>>();

	public SLRAnalyserTable() {

	}

	public List<List<Lattice>> getACTION() {
		return ACTION;
	}

	public void setACTION(List<List<Lattice>> aCTION) {
		ACTION = aCTION;
	}

	public List<List<Lattice>> getGOTO() {
		return GOTO;
	}

	public void setGOTO(List<List<Lattice>> gOTO) {
		GOTO = gOTO;
	}

	public SLRAnalyserTable(Grammar G, ItemFamily itemFamily) {
		try {
			createSLRAnalyserTable(G, itemFamily);
		} catch (SRException e) {
			System.out.println(e.error);
			System.exit(0);
		}
	}

	public void createSLRAnalyserTable(Grammar G, ItemFamily itemFamily) throws SRException {
		// 先把终结符和非终结符保存一下
		List<String> non_terminal = G.getNon_terminals();
		List<String> terminal = G.getTerminals();
		List<Follow> follows = G.getFollows();
		terminal.add("$");
		// 为action表和goto表分配空间
		for (int i = 0; i < itemFamily.itemsets.size(); i++) {
			// 为action表分配空间
			List<Lattice> list1 = new ArrayList<Lattice>();
			for (int j = 0; j < terminal.size(); j++) {
				Lattice lattice = new Lattice();
				list1.add(lattice);
			}
			// 为goto表分配空间
			List<Lattice> list2 = new ArrayList<Lattice>();
			for (int j = 0; j < non_terminal.size(); j++) {
				Lattice lattice = new Lattice();
				list2.add(lattice);
			}
			ACTION.add(list1);
			GOTO.add(list2);
		}
		// 生成SLR分析表
		for (int i = 0; i < itemFamily.itemsets.size(); i++) {
			for (int j = 0; j < itemFamily.itemsets.get(i).items.size(); j++) {
				Item item = itemFamily.itemsets.get(i).items.get(j);
				if (item.point < item.rightParts.size()) {// 点不在产生式右部的最后一个位置，类似E->E·+T
					if (terminal.contains(item.rightParts.get(item.point))) {// 如果点后面的是终结符，类似类似E->E·+T
						int location = terminal.indexOf(item.rightParts.get(item.point));// 用location表示点后面的终结符在终结符集合中的位置
						if (ACTION.get(i).get(location).action != 'r' && ACTION.get(i).get(location).action != 's') {
							ACTION.get(i).get(location).action = 's';
						} else if (ACTION.get(i).get(location).action == 'r') {
							//throw new SRException(1);
							//运算符优先级规则: ^ > *、/ > +、-
							//if...else.../if... 优先级规则: 当到达if...状态时下一个字符为else则移入，否则规约
							if ((i == 44 || i == 45)) {//加减法
								if((terminal.get(location).equals("+")
									||terminal.get(location).equals("-")
									)) {
									ACTION.get(i).get(location).action='r';
									if(i==44)
										ACTION.get(i).get(location).number=18;
									else
										ACTION.get(i).get(location).number=19;
								}else if (terminal.get(location).equals("*")) {
									ACTION.get(i).get(location).action='s';
									ACTION.get(i).get(location).number=35;
								}else if(terminal.get(location).equals("/")) {
									ACTION.get(i).get(location).action='s';
									ACTION.get(i).get(location).number=36;
								}else if(terminal.get(location).equals("^")) {
									ACTION.get(i).get(location).action='s';
									ACTION.get(i).get(location).number=37;
								}
							}else if ( i == 46 || i == 47 ) {//乘除法
								if(terminal.get(location).equals("^")) {
									ACTION.get(i).get(location).action='s';
									ACTION.get(i).get(location).number=37;
								}else if (i==46) {
									ACTION.get(i).get(location).action='r';
									ACTION.get(i).get(location).number=20;
								}else if(i==47) {
									ACTION.get(i).get(location).action='r';
									ACTION.get(i).get(location).number=21;
								}
							}else if(terminal.get(location).equals("^")) {//指数运算
								ACTION.get(i).get(location).action='s';
								ACTION.get(i).get(location).number=37;
							}else if (terminal.get(location).equals("else")) {//if...else.../if...
								ACTION.get(i).get(location).action='s';
								ACTION.get(i).get(location).number=52;/*这里*/
							}
							continue;
						}
						for (int k = 1; k < itemFamily.itemsets.size(); k++) {
							Item k1item = itemFamily.itemsets.get(k).items.get(0);
							Item ijitem = itemFamily.itemsets.get(i).items.get(j);
							if (k1item.productionEquals(ijitem) && k1item.point == ijitem.point + 1) {
								ACTION.get(i).get(location).number = k;
								break;
							}
						}
					} else {// 如果点后面的字符是非终结符，类似E->E+·T
						int location = non_terminal.indexOf(item.rightParts.get(item.point));// 用location表示点后面的非终结符在非终结符集合中的位置
						for (int k = 1; k < itemFamily.itemsets.size(); k++) {
							Item k1item = itemFamily.itemsets.get(k).items.get(0);
							Item ijitem = itemFamily.itemsets.get(i).items.get(j);
							if (k1item.productionEquals(ijitem) && k1item.point == ijitem.point + 1) {
								GOTO.get(i).get(location).number = k;
								break;
							}
						}
					}
				} else if (item.point == item.rightParts.size()) {// 点在产生式右部的最后一个位置，类似E->E+T·
					if (item.leftPart.equals("START")) { // 如果左部是START拓广文法，就在action表中对应位置加入acc。因为使用char型字符数组保存的，因此这里保存a
						int location = terminal.indexOf("$");
						ACTION.get(i).get(location).action = 'a';
					} else {
						Follow follow = follows.get(non_terminal.indexOf(item.leftPart));// E->T· 将
						for (int k = 0; k < follow.getfollow().size(); k++) {
							int location = terminal.indexOf(follow.getfollow().get(k));
							if (ACTION.get(i).get(location).action != 'r'
									&& ACTION.get(i).get(location).action != 's') {
								ACTION.get(i).get(location).action = 'r';
								ACTION.get(i).get(location).number = item.number;
							} else if (ACTION.get(i).get(location).action == 's') {
								throw new SRException(1);
							} else {
								throw new SRException(2);
							}
						}
					}
				}
			}
		}
//		GOTO.get(4).get(Grammar.non_terminals.indexOf("compoundstmt")).number = 8;
		// 输出SLR预测分析表
		SLRAnalyserTable.display_Table();
	}
	public static void display_Table() {
		System.out.println("\n预测分析表：");
		for (int i = 0; i < Grammar.terminals.size(); i++) {
			System.out.print("\t" + Grammar.terminals.get(i));
		}
		for (int i = 0; i < Grammar.non_terminals.size(); i++) {
			System.out.print("\t" + Grammar.non_terminals.get(i));
		}
		System.out.println();
		for (int i = 0; i < ACTION.size(); i++) {
			System.out.print(i);
			for (int j = 0; j < ACTION.get(i).size(); j++) {
				System.out.print("\t" + ACTION.get(i).get(j).action + ACTION.get(i).get(j).number);
				if (ACTION.get(i).get(j).error!=0) {
					System.out.print("/"+ACTION.get(i).get(j).Action.action+ACTION.get(i).get(j).Action.number);
				}
			}
			for (int j = 0; j < GOTO.get(i).size(); j++) {
				System.out.print("\t" + GOTO.get(i).get(j).number);
			}
			System.out.println();
		}
	}
	public void print() {

	}
}
