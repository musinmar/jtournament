package com.clocktower.tournament;

import static com.clocktower.tournament.domain.Nation.ALMAGEST;
import static com.clocktower.tournament.domain.Nation.BELLEROFON;
import static com.clocktower.tournament.domain.Nation.GALILEO;
import static com.clocktower.tournament.domain.Nation.KAMELEOPARD;
import static com.clocktower.tournament.domain.Nation.OBERON_22;
import static com.clocktower.tournament.domain.Title.COMMON;
import static com.clocktower.tournament.domain.Title.LORD;
import static com.clocktower.tournament.domain.Title.SIR;

public class DefaultData {

    public static Player[] initDefaultPlayers() {
        Player[] kn = new Player[30];
        for (int i = 0; i < kn.length; i++) {
            kn[i] = new Player();
            kn[i].id = i;
        }

        kn[dec(1)].setName("Fler");
        kn[dec(1)].setSurname("Rokky");
        kn[dec(1)].setTitle(LORD);
        kn[dec(1)].setNation(ALMAGEST);
        kn[dec(1)].setTown("Aldorum");
        kn[dec(1)].setAt(6);
        kn[dec(1)].setDef(4);
        kn[dec(1)].setPersistentLevel(9);
        kn[dec(1)].setS(15);
        kn[dec(1)].setL(14);
        kn[dec(1)].setV(13);

        kn[dec(2)].setName("Johnny");
        kn[dec(2)].setSurname("Wolf");
        kn[dec(2)].setTitle(SIR);
        kn[dec(2)].setNation(ALMAGEST);
        kn[dec(2)].setTown("Murahir");
        kn[dec(2)].setAt(4);
        kn[dec(2)].setDef(6);
        kn[dec(2)].setPersistentLevel(8);
        kn[dec(2)].setS(14);
        kn[dec(2)].setL(15);
        kn[dec(2)].setV(12);

        kn[dec(3)].setName("Dick");
        kn[dec(3)].setSurname("Rendell");
        kn[dec(3)].setTitle(COMMON);
        kn[dec(3)].setNation(ALMAGEST);
        kn[dec(3)].setTown("Linagor");
        kn[dec(3)].setAt(5);
        kn[dec(3)].setDef(5);
        kn[dec(3)].setPersistentLevel(4);
        kn[dec(3)].setS(11);
        kn[dec(3)].setL(12);
        kn[dec(3)].setV(14);

        kn[dec(4)].setName("Bert");
        kn[dec(4)].setSurname("Basky");
        kn[dec(4)].setTitle(COMMON);
        kn[dec(4)].setNation(ALMAGEST);
        kn[dec(4)].setTown("Medlur");
        kn[dec(4)].setAt(8);
        kn[dec(4)].setDef(2);
        kn[dec(4)].setPersistentLevel(5);
        kn[dec(4)].setS(14);
        kn[dec(4)].setL(12);
        kn[dec(4)].setV(12);

        kn[dec(5)].setName("Nick");
        kn[dec(5)].setSurname("Toffer");
        kn[dec(5)].setTitle(COMMON);
        kn[dec(5)].setNation(ALMAGEST);
        kn[dec(5)].setTown("Telmir");
        kn[dec(5)].setAt(7);
        kn[dec(5)].setDef(3);
        kn[dec(5)].setPersistentLevel(3);
        kn[dec(5)].setS(12);
        kn[dec(5)].setL(12);
        kn[dec(5)].setV(12);

        kn[dec(6)].setName("Steve");
        kn[dec(6)].setSurname("Ashenbach");
        kn[dec(6)].setTitle(COMMON);
        kn[dec(6)].setNation(ALMAGEST);
        kn[dec(6)].setTown("Turon");
        kn[dec(6)].setAt(4);
        kn[dec(6)].setDef(6);
        kn[dec(6)].setPersistentLevel(2);
        kn[dec(6)].setS(12);
        kn[dec(6)].setL(11);
        kn[dec(6)].setV(12);

        kn[dec(7)].setName("Den");
        kn[dec(7)].setSurname("Shadow");
        kn[dec(7)].setTitle(SIR);
        kn[dec(7)].setNation(BELLEROFON);
        kn[dec(7)].setTown("Linagor");
        kn[dec(7)].setAt(5);
        kn[dec(7)].setDef(5);
        kn[dec(7)].setPersistentLevel(8);
        kn[dec(7)].setS(14);
        kn[dec(7)].setL(13);
        kn[dec(7)].setV(14);

        kn[dec(8)].setName("Glen");
        kn[dec(8)].setSurname("Zutter");
        kn[dec(8)].setTitle(SIR);
        kn[dec(8)].setNation(BELLEROFON);
        kn[dec(8)].setTown("Linagor");
        kn[dec(8)].setAt(5);
        kn[dec(8)].setDef(5);
        kn[dec(8)].setPersistentLevel(7);
        kn[dec(8)].setS(13);
        kn[dec(8)].setL(14);
        kn[dec(8)].setV(13);

        kn[dec(9)].setName("Willy");
        kn[dec(9)].setSurname("Parker");
        kn[dec(9)].setTitle(COMMON);
        kn[dec(9)].setNation(BELLEROFON);
        kn[dec(9)].setTown("Ohrun");
        kn[dec(9)].setAt(7);
        kn[dec(9)].setDef(3);
        kn[dec(9)].setPersistentLevel(5);
        kn[dec(9)].setS(12);
        kn[dec(9)].setL(13);
        kn[dec(9)].setV(13);

        kn[dec(10)].setName("Sindy");
        kn[dec(10)].setSurname("Merick");
        kn[dec(10)].setTitle(COMMON);
        kn[dec(10)].setNation(BELLEROFON);
        kn[dec(10)].setTown("Turon");
        kn[dec(10)].setAt(8);
        kn[dec(10)].setDef(2);
        kn[dec(10)].setPersistentLevel(5);
        kn[dec(10)].setS(15);
        kn[dec(10)].setL(11);
        kn[dec(10)].setV(12);

        kn[dec(11)].setName("Arly");
        kn[dec(11)].setSurname("Chiko");
        kn[dec(11)].setTitle(COMMON);
        kn[dec(11)].setNation(BELLEROFON);
        kn[dec(11)].setTown("Reldor");
        kn[dec(11)].setAt(3);
        kn[dec(11)].setDef(7);
        kn[dec(11)].setPersistentLevel(4);
        kn[dec(11)].setS(13);
        kn[dec(11)].setL(12);
        kn[dec(11)].setV(12);

        kn[dec(12)].setName("Archy");
        kn[dec(12)].setSurname("Soks");
        kn[dec(12)].setTitle(COMMON);
        kn[dec(12)].setNation(BELLEROFON);
        kn[dec(12)].setTown("Turon");
        kn[dec(12)].setAt(1);
        kn[dec(12)].setDef(9);
        kn[dec(12)].setPersistentLevel(3);
        kn[dec(12)].setS(11);
        kn[dec(12)].setL(14);
        kn[dec(12)].setV(11);

        kn[dec(13)].setName("Michael");
        kn[dec(13)].setSurname("Holl");
        kn[dec(13)].setTitle(LORD);
        kn[dec(13)].setNation(GALILEO);
        kn[dec(13)].setTown("Ejmoril");
        kn[dec(13)].setAt(1);
        kn[dec(13)].setDef(9);
        kn[dec(13)].setPersistentLevel(10);
        kn[dec(13)].setS(13);
        kn[dec(13)].setL(17);
        kn[dec(13)].setV(14);

        kn[dec(14)].setName("Antony");
        kn[dec(14)].setSurname("Baks");
        kn[dec(14)].setTitle(COMMON);
        kn[dec(14)].setNation(GALILEO);
        kn[dec(14)].setTown("Rajzer");
        kn[dec(14)].setAt(6);
        kn[dec(14)].setDef(4);
        kn[dec(14)].setPersistentLevel(6);
        kn[dec(14)].setS(15);
        kn[dec(14)].setL(13);
        kn[dec(14)].setV(11);

        kn[dec(15)].setName("Sally");
        kn[dec(15)].setSurname("Hunter");
        kn[dec(15)].setTitle(COMMON);
        kn[dec(15)].setNation(GALILEO);
        kn[dec(15)].setTown("Telmir");
        kn[dec(15)].setAt(8);
        kn[dec(15)].setDef(2);
        kn[dec(15)].setPersistentLevel(5);
        kn[dec(15)].setS(13);
        kn[dec(15)].setL(13);
        kn[dec(15)].setV(11);

        kn[dec(16)].setName("Phil");
        kn[dec(16)].setSurname("Kukky");
        kn[dec(16)].setTitle(COMMON);
        kn[dec(16)].setNation(GALILEO);
        kn[dec(16)].setTown("Shiragon");
        kn[dec(16)].setAt(3);
        kn[dec(16)].setDef(7);
        kn[dec(16)].setPersistentLevel(3);
        kn[dec(16)].setS(11);
        kn[dec(16)].setL(11);
        kn[dec(16)].setV(14);

        kn[dec(17)].setName("Jack");
        kn[dec(17)].setSurname("Richy");
        kn[dec(17)].setTitle(COMMON);
        kn[dec(17)].setNation(GALILEO);
        kn[dec(17)].setTown("Siligun");
        kn[dec(17)].setAt(1);
        kn[dec(17)].setDef(9);
        kn[dec(17)].setPersistentLevel(1);
        kn[dec(17)].setS(12);
        kn[dec(17)].setL(11);
        kn[dec(17)].setV(11);

        kn[dec(18)].setName("Paola");
        kn[dec(18)].setSurname("Fozzi");
        kn[dec(18)].setTitle(COMMON);
        kn[dec(18)].setNation(GALILEO);
        kn[dec(18)].setTown("Ejmoril");
        kn[dec(18)].setAt(8);
        kn[dec(18)].setDef(2);
        kn[dec(18)].setPersistentLevel(1);
        kn[dec(18)].setS(11);
        kn[dec(18)].setL(12);
        kn[dec(18)].setV(11);

        kn[dec(19)].setName("Chen");
        kn[dec(19)].setSurname("Fletcher");
        kn[dec(19)].setTitle(LORD);
        kn[dec(19)].setNation(KAMELEOPARD);
        kn[dec(19)].setTown("Irif");
        kn[dec(19)].setAt(7);
        kn[dec(19)].setDef(3);
        kn[dec(19)].setPersistentLevel(10);
        kn[dec(19)].setS(13);
        kn[dec(19)].setL(17);
        kn[dec(19)].setV(13);

        kn[dec(20)].setName("Tanya");
        kn[dec(20)].setSurname("Dzhoko");
        kn[dec(20)].setTitle(SIR);
        kn[dec(20)].setNation(KAMELEOPARD);
        kn[dec(20)].setTown("Reldor");
        kn[dec(20)].setAt(6);
        kn[dec(20)].setDef(4);
        kn[dec(20)].setPersistentLevel(8);
        kn[dec(20)].setS(12);
        kn[dec(20)].setL(15);
        kn[dec(20)].setV(14);

        kn[dec(21)].setName("Bob");
        kn[dec(21)].setSurname("Drou");
        kn[dec(21)].setTitle(COMMON);
        kn[dec(21)].setNation(KAMELEOPARD);
        kn[dec(21)].setTown("Irif");
        kn[dec(21)].setAt(4);
        kn[dec(21)].setDef(6);
        kn[dec(21)].setPersistentLevel(6);
        kn[dec(21)].setS(15);
        kn[dec(21)].setL(11);
        kn[dec(21)].setV(13);

        kn[dec(22)].setName("Klod");
        kn[dec(22)].setSurname("Gosh");
        kn[dec(22)].setTitle(COMMON);
        kn[dec(22)].setNation(KAMELEOPARD);
        kn[dec(22)].setTown("Dilion");
        kn[dec(22)].setAt(7);
        kn[dec(22)].setDef(3);
        kn[dec(22)].setPersistentLevel(6);
        kn[dec(22)].setS(13);
        kn[dec(22)].setL(13);
        kn[dec(22)].setV(13);

        kn[dec(23)].setName("Mary");
        kn[dec(23)].setSurname("Kullidg");
        kn[dec(23)].setTitle(COMMON);
        kn[dec(23)].setNation(KAMELEOPARD);
        kn[dec(23)].setTown("Gor");
        kn[dec(23)].setAt(9);
        kn[dec(23)].setDef(1);
        kn[dec(23)].setPersistentLevel(4);
        kn[dec(23)].setS(14);
        kn[dec(23)].setL(11);
        kn[dec(23)].setV(11);

        kn[dec(24)].setName("Jeff");
        kn[dec(24)].setSurname("Ringo");
        kn[dec(24)].setTitle(COMMON);
        kn[dec(24)].setNation(KAMELEOPARD);
        kn[dec(24)].setTown("Irif");
        kn[dec(24)].setAt(2);
        kn[dec(24)].setDef(8);
        kn[dec(24)].setPersistentLevel(3);
        kn[dec(24)].setS(12);
        kn[dec(24)].setL(13);
        kn[dec(24)].setV(11);

        kn[dec(25)].setName("Leo");
        kn[dec(25)].setSurname("Leng");
        kn[dec(25)].setTitle(LORD);
        kn[dec(25)].setNation(OBERON_22);
        kn[dec(25)].setTown("Julmar");
        kn[dec(25)].setAt(3);
        kn[dec(25)].setDef(7);
        kn[dec(25)].setPersistentLevel(9);
        kn[dec(25)].setS(17);
        kn[dec(25)].setL(12);
        kn[dec(25)].setV(13);

        kn[dec(26)].setName("May");
        kn[dec(26)].setSurname("Klaps");
        kn[dec(26)].setTitle(SIR);
        kn[dec(26)].setNation(OBERON_22);
        kn[dec(26)].setTown("Alior");
        kn[dec(26)].setAt(7);
        kn[dec(26)].setDef(3);
        kn[dec(26)].setPersistentLevel(7);
        kn[dec(26)].setS(13);
        kn[dec(26)].setL(15);
        kn[dec(26)].setV(12);

        kn[dec(27)].setName("Pat");
        kn[dec(27)].setSurname("Kent");
        kn[dec(27)].setTitle(COMMON);
        kn[dec(27)].setNation(OBERON_22);
        kn[dec(27)].setTown("Dilion");
        kn[dec(27)].setAt(4);
        kn[dec(27)].setDef(6);
        kn[dec(27)].setPersistentLevel(7);
        kn[dec(27)].setS(15);
        kn[dec(27)].setL(12);
        kn[dec(27)].setV(13);

        kn[dec(28)].setName("Mikky");
        kn[dec(28)].setSurname("Shuf");
        kn[dec(28)].setTitle(SIR);
        kn[dec(28)].setNation(OBERON_22);
        kn[dec(28)].setTown("Alior");
        kn[dec(28)].setAt(6);
        kn[dec(28)].setDef(4);
        kn[dec(28)].setPersistentLevel(7);
        kn[dec(28)].setS(12);
        kn[dec(28)].setL(12);
        kn[dec(28)].setV(16);

        kn[dec(29)].setName("Jho");
        kn[dec(29)].setSurname("Gugi");
        kn[dec(29)].setTitle(COMMON);
        kn[dec(29)].setNation(OBERON_22);
        kn[dec(29)].setTown("Alior");
        kn[dec(29)].setAt(2);
        kn[dec(29)].setDef(8);
        kn[dec(29)].setPersistentLevel(2);
        kn[dec(29)].setS(13);
        kn[dec(29)].setL(11);
        kn[dec(29)].setV(11);

        kn[dec(30)].setName("Dino");
        kn[dec(30)].setSurname("Nensy");
        kn[dec(30)].setTitle(COMMON);
        kn[dec(30)].setNation(OBERON_22);
        kn[dec(30)].setTown("Itejro");
        kn[dec(30)].setAt(5);
        kn[dec(30)].setDef(5);
        kn[dec(30)].setPersistentLevel(2);
        kn[dec(30)].setS(11);
        kn[dec(30)].setL(12);
        kn[dec(30)].setV(12);

        initDecks(kn);

        return kn;
    }

    private static void initDecks(Player[] kn) {
        kn[dec(1)].deckKind[0] = 1;
        kn[dec(1)].deckKind[1] = 8;

        kn[dec(2)].deckKind[0] = 1;
        kn[dec(2)].deckKind[1] = 2;

        kn[dec(3)].deckKind[0] = 2;
        kn[dec(3)].deckKind[1] = 9;

        kn[dec(4)].deckKind[0] = 4;
        kn[dec(4)].deckKind[1] = 5;

        kn[dec(5)].deckKind[0] = 6;
        kn[dec(5)].deckKind[1] = 9;

        kn[dec(6)].deckKind[0] = 2;
        kn[dec(6)].deckKind[1] = 4;

        kn[dec(7)].deckKind[0] = 4;
        kn[dec(7)].deckKind[1] = 9;

        kn[dec(8)].deckKind[0] = 6;
        kn[dec(8)].deckKind[1] = 8;

        kn[dec(9)].deckKind[0] = 5;
        kn[dec(9)].deckKind[1] = 8;

        kn[dec(10)].deckKind[0] = 3;
        kn[dec(10)].deckKind[1] = 5;

        kn[dec(11)].deckKind[0] = 3;
        kn[dec(11)].deckKind[1] = 6;

        kn[dec(12)].deckKind[0] = 2;
        kn[dec(12)].deckKind[1] = 3;

        kn[dec(13)].deckKind[0] = 3;
        kn[dec(13)].deckKind[1] = 4;

        kn[dec(14)].deckKind[0] = 4;
        kn[dec(14)].deckKind[1] = 7;

        kn[dec(15)].deckKind[0] = 1;
        kn[dec(15)].deckKind[1] = 3;

        kn[dec(16)].deckKind[0] = 1;
        kn[dec(16)].deckKind[1] = 5;

        kn[dec(17)].deckKind[0] = 4;
        kn[dec(17)].deckKind[1] = 8;

        kn[dec(18)].deckKind[0] = 5;
        kn[dec(18)].deckKind[1] = 7;

        kn[dec(19)].deckKind[0] = 2;
        kn[dec(19)].deckKind[1] = 7;

        kn[dec(20)].deckKind[0] = 6;
        kn[dec(20)].deckKind[1] = 7;

        kn[dec(21)].deckKind[0] = 1;
        kn[dec(21)].deckKind[1] = 6;

        kn[dec(22)].deckKind[0] = 1;
        kn[dec(22)].deckKind[1] = 4;

        kn[dec(23)].deckKind[0] = 4;
        kn[dec(23)].deckKind[1] = 6;

        kn[dec(24)].deckKind[0] = 5;
        kn[dec(24)].deckKind[1] = 9;

        kn[dec(25)].deckKind[0] = 3;
        kn[dec(25)].deckKind[1] = 8;

        kn[dec(26)].deckKind[0] = 2;
        kn[dec(26)].deckKind[1] = 8;

        kn[dec(27)].deckKind[0] = 2;
        kn[dec(27)].deckKind[1] = 5;

        kn[dec(28)].deckKind[0] = 7;
        kn[dec(28)].deckKind[1] = 9;

        kn[dec(29)].deckKind[0] = 2;
        kn[dec(29)].deckKind[1] = 6;

        kn[dec(30)].deckKind[0] = 5;
        kn[dec(30)].deckKind[1] = 6;
    }

    private static int dec(int v) {
        return v - 1;
    }
}
