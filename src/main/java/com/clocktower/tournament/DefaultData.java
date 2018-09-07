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

    public static void initDefaultPlayers(Player[] kn) {
        for (int i = 0; i < kn.length; i++) {
            kn[i].id = i;
        }

        kn[dec(1)].name = "Fler";
        kn[dec(1)].surname = "Rokky";
        kn[dec(1)].setTitle(LORD);
        kn[dec(1)].setNation(ALMAGEST);
        kn[dec(1)].town = "Aldorum";
        kn[dec(1)].at = 6;
        kn[dec(1)].def = 4;
        kn[dec(1)].level = 9;
        kn[dec(1)].s = 15;
        kn[dec(1)].l = 14;
        kn[dec(1)].v = 13;

        kn[dec(2)].name = "Johnny";
        kn[dec(2)].surname = "Wolf";
        kn[dec(2)].setTitle(SIR);
        kn[dec(2)].setNation(ALMAGEST);
        kn[dec(2)].town = "Murahir";
        kn[dec(2)].at = 4;
        kn[dec(2)].def = 6;
        kn[dec(2)].level = 8;
        kn[dec(2)].s = 14;
        kn[dec(2)].l = 15;
        kn[dec(2)].v = 12;

        kn[dec(3)].name = "Dick";
        kn[dec(3)].surname = "Rendell";
        kn[dec(3)].setTitle(COMMON);
        kn[dec(3)].setNation(ALMAGEST);
        kn[dec(3)].town = "Linagor";
        kn[dec(3)].at = 5;
        kn[dec(3)].def = 5;
        kn[dec(3)].level = 4;
        kn[dec(3)].s = 11;
        kn[dec(3)].l = 12;
        kn[dec(3)].v = 14;

        kn[dec(4)].name = "Bert";
        kn[dec(4)].surname = "Basky";
        kn[dec(4)].setTitle(COMMON);
        kn[dec(4)].setNation(ALMAGEST);
        kn[dec(4)].town = "Medlur";
        kn[dec(4)].at = 8;
        kn[dec(4)].def = 2;
        kn[dec(4)].level = 5;
        kn[dec(4)].s = 14;
        kn[dec(4)].l = 12;
        kn[dec(4)].v = 12;

        kn[dec(5)].name = "Nick";
        kn[dec(5)].surname = "Toffer";
        kn[dec(5)].setTitle(COMMON);
        kn[dec(5)].setNation(ALMAGEST);
        kn[dec(5)].town = "Telmir";
        kn[dec(5)].at = 7;
        kn[dec(5)].def = 3;
        kn[dec(5)].level = 3;
        kn[dec(5)].s = 12;
        kn[dec(5)].l = 12;
        kn[dec(5)].v = 12;

        kn[dec(6)].name = "Steve";
        kn[dec(6)].surname = "Ashenbach";
        kn[dec(6)].setTitle(COMMON);
        kn[dec(6)].setNation(ALMAGEST);
        kn[dec(6)].town = "Turon";
        kn[dec(6)].at = 4;
        kn[dec(6)].def = 6;
        kn[dec(6)].level = 2;
        kn[dec(6)].s = 12;
        kn[dec(6)].l = 11;
        kn[dec(6)].v = 12;

        kn[dec(7)].name = "Den";
        kn[dec(7)].surname = "Shadow";
        kn[dec(7)].setTitle(SIR);
        kn[dec(7)].setNation(BELLEROFON);
        kn[dec(7)].town = "Linagor";
        kn[dec(7)].at = 5;
        kn[dec(7)].def = 5;
        kn[dec(7)].level = 8;
        kn[dec(7)].s = 14;
        kn[dec(7)].l = 13;
        kn[dec(7)].v = 14;

        kn[dec(8)].name = "Glen";
        kn[dec(8)].surname = "Zutter";
        kn[dec(8)].setTitle(SIR);
        kn[dec(8)].setNation(BELLEROFON);
        kn[dec(8)].town = "Linagor";
        kn[dec(8)].at = 5;
        kn[dec(8)].def = 5;
        kn[dec(8)].level = 7;
        kn[dec(8)].s = 13;
        kn[dec(8)].l = 14;
        kn[dec(8)].v = 13;

        kn[dec(9)].name = "Willy";
        kn[dec(9)].surname = "Parker";
        kn[dec(9)].setTitle(COMMON);
        kn[dec(9)].setNation(BELLEROFON);
        kn[dec(9)].town = "Ohrun";
        kn[dec(9)].at = 7;
        kn[dec(9)].def = 3;
        kn[dec(9)].level = 5;
        kn[dec(9)].s = 12;
        kn[dec(9)].l = 13;
        kn[dec(9)].v = 13;

        kn[dec(10)].name = "Sindy";
        kn[dec(10)].surname = "Merick";
        kn[dec(10)].setTitle(COMMON);
        kn[dec(10)].setNation(BELLEROFON);
        kn[dec(10)].town = "Turon";
        kn[dec(10)].at = 8;
        kn[dec(10)].def = 2;
        kn[dec(10)].level = 5;
        kn[dec(10)].s = 15;
        kn[dec(10)].l = 11;
        kn[dec(10)].v = 12;

        kn[dec(11)].name = "Arly";
        kn[dec(11)].surname = "Chiko";
        kn[dec(11)].setTitle(COMMON);
        kn[dec(11)].setNation(BELLEROFON);
        kn[dec(11)].town = "Reldor";
        kn[dec(11)].at = 3;
        kn[dec(11)].def = 7;
        kn[dec(11)].level = 4;
        kn[dec(11)].s = 13;
        kn[dec(11)].l = 12;
        kn[dec(11)].v = 12;

        kn[dec(12)].name = "Archy";
        kn[dec(12)].surname = "Soks";
        kn[dec(12)].setTitle(COMMON);
        kn[dec(12)].setNation(BELLEROFON);
        kn[dec(12)].town = "Turon";
        kn[dec(12)].at = 1;
        kn[dec(12)].def = 9;
        kn[dec(12)].level = 3;
        kn[dec(12)].s = 11;
        kn[dec(12)].l = 14;
        kn[dec(12)].v = 11;

        kn[dec(13)].name = "Michael";
        kn[dec(13)].surname = "Holl";
        kn[dec(13)].setTitle(LORD);
        kn[dec(13)].setNation(GALILEO);
        kn[dec(13)].town = "Ejmoril";
        kn[dec(13)].at = 1;
        kn[dec(13)].def = 9;
        kn[dec(13)].level = 10;
        kn[dec(13)].s = 13;
        kn[dec(13)].l = 17;
        kn[dec(13)].v = 14;

        kn[dec(14)].name = "Antony";
        kn[dec(14)].surname = "Baks";
        kn[dec(14)].setTitle(COMMON);
        kn[dec(14)].setNation(GALILEO);
        kn[dec(14)].town = "Rajzer";
        kn[dec(14)].at = 6;
        kn[dec(14)].def = 4;
        kn[dec(14)].level = 6;
        kn[dec(14)].s = 15;
        kn[dec(14)].l = 13;
        kn[dec(14)].v = 11;

        kn[dec(15)].name = "Sally";
        kn[dec(15)].surname = "Hunter";
        kn[dec(15)].setTitle(COMMON);
        kn[dec(15)].setNation(GALILEO);
        kn[dec(15)].town = "Telmir";
        kn[dec(15)].at = 8;
        kn[dec(15)].def = 2;
        kn[dec(15)].level = 5;
        kn[dec(15)].s = 13;
        kn[dec(15)].l = 13;
        kn[dec(15)].v = 11;

        kn[dec(16)].name = "Phil";
        kn[dec(16)].surname = "Kukky";
        kn[dec(16)].setTitle(COMMON);
        kn[dec(16)].setNation(GALILEO);
        kn[dec(16)].town = "Shiragon";
        kn[dec(16)].at = 3;
        kn[dec(16)].def = 7;
        kn[dec(16)].level = 3;
        kn[dec(16)].s = 11;
        kn[dec(16)].l = 11;
        kn[dec(16)].v = 14;

        kn[dec(17)].name = "Jack";
        kn[dec(17)].surname = "Richy";
        kn[dec(17)].setTitle(COMMON);
        kn[dec(17)].setNation(GALILEO);
        kn[dec(17)].town = "Siligun";
        kn[dec(17)].at = 1;
        kn[dec(17)].def = 9;
        kn[dec(17)].level = 1;
        kn[dec(17)].s = 12;
        kn[dec(17)].l = 11;
        kn[dec(17)].v = 11;

        kn[dec(18)].name = "Paola";
        kn[dec(18)].surname = "Fozzi";
        kn[dec(18)].setTitle(COMMON);
        kn[dec(18)].setNation(GALILEO);
        kn[dec(18)].town = "Ejmoril";
        kn[dec(18)].at = 8;
        kn[dec(18)].def = 2;
        kn[dec(18)].level = 1;
        kn[dec(18)].s = 11;
        kn[dec(18)].l = 12;
        kn[dec(18)].v = 11;

        kn[dec(19)].name = "Chen";
        kn[dec(19)].surname = "Fletcher";
        kn[dec(19)].setTitle(LORD);
        kn[dec(19)].setNation(KAMELEOPARD);
        kn[dec(19)].town = "Irif";
        kn[dec(19)].at = 7;
        kn[dec(19)].def = 3;
        kn[dec(19)].level = 10;
        kn[dec(19)].s = 13;
        kn[dec(19)].l = 17;
        kn[dec(19)].v = 13;

        kn[dec(20)].name = "Tanya";
        kn[dec(20)].surname = "Dzhoko";
        kn[dec(20)].setTitle(SIR);
        kn[dec(20)].setNation(KAMELEOPARD);
        kn[dec(20)].town = "Reldor";
        kn[dec(20)].at = 6;
        kn[dec(20)].def = 4;
        kn[dec(20)].level = 8;
        kn[dec(20)].s = 12;
        kn[dec(20)].l = 15;
        kn[dec(20)].v = 14;

        kn[dec(21)].name = "Bob";
        kn[dec(21)].surname = "Drou";
        kn[dec(21)].setTitle(COMMON);
        kn[dec(21)].setNation(KAMELEOPARD);
        kn[dec(21)].town = "Irif";
        kn[dec(21)].at = 4;
        kn[dec(21)].def = 6;
        kn[dec(21)].level = 6;
        kn[dec(21)].s = 15;
        kn[dec(21)].l = 11;
        kn[dec(21)].v = 13;

        kn[dec(22)].name = "Klod";
        kn[dec(22)].surname = "Gosh";
        kn[dec(22)].setTitle(COMMON);
        kn[dec(22)].setNation(KAMELEOPARD);
        kn[dec(22)].town = "Dilion";
        kn[dec(22)].at = 7;
        kn[dec(22)].def = 3;
        kn[dec(22)].level = 6;
        kn[dec(22)].s = 13;
        kn[dec(22)].l = 13;
        kn[dec(22)].v = 13;

        kn[dec(23)].name = "Mary";
        kn[dec(23)].surname = "Kullidg";
        kn[dec(23)].setTitle(COMMON);
        kn[dec(23)].setNation(KAMELEOPARD);
        kn[dec(23)].town = "Gor";
        kn[dec(23)].at = 9;
        kn[dec(23)].def = 1;
        kn[dec(23)].level = 4;
        kn[dec(23)].s = 14;
        kn[dec(23)].l = 11;
        kn[dec(23)].v = 11;

        kn[dec(24)].name = "Jeff";
        kn[dec(24)].surname = "Ringo";
        kn[dec(24)].setTitle(COMMON);
        kn[dec(24)].setNation(KAMELEOPARD);
        kn[dec(24)].town = "Irif";
        kn[dec(24)].at = 2;
        kn[dec(24)].def = 8;
        kn[dec(24)].level = 3;
        kn[dec(24)].s = 12;
        kn[dec(24)].l = 13;
        kn[dec(24)].v = 11;

        kn[dec(25)].name = "Leo";
        kn[dec(25)].surname = "Leng";
        kn[dec(25)].setTitle(LORD);
        kn[dec(25)].setNation(OBERON_22);
        kn[dec(25)].town = "Julmar";
        kn[dec(25)].at = 3;
        kn[dec(25)].def = 7;
        kn[dec(25)].level = 9;
        kn[dec(25)].s = 17;
        kn[dec(25)].l = 12;
        kn[dec(25)].v = 13;

        kn[dec(26)].name = "May";
        kn[dec(26)].surname = "Klaps";
        kn[dec(26)].setTitle(SIR);
        kn[dec(26)].setNation(OBERON_22);
        kn[dec(26)].town = "Alior";
        kn[dec(26)].at = 7;
        kn[dec(26)].def = 3;
        kn[dec(26)].level = 7;
        kn[dec(26)].s = 13;
        kn[dec(26)].l = 15;
        kn[dec(26)].v = 12;

        kn[dec(27)].name = "Pat";
        kn[dec(27)].surname = "Kent";
        kn[dec(27)].setTitle(COMMON);
        kn[dec(27)].setNation(OBERON_22);
        kn[dec(27)].town = "Dilion";
        kn[dec(27)].at = 4;
        kn[dec(27)].def = 6;
        kn[dec(27)].level = 7;
        kn[dec(27)].s = 15;
        kn[dec(27)].l = 12;
        kn[dec(27)].v = 13;

        kn[dec(28)].name = "Mikky";
        kn[dec(28)].surname = "Shuf";
        kn[dec(28)].setTitle(SIR);
        kn[dec(28)].setNation(OBERON_22);
        kn[dec(28)].town = "Alior";
        kn[dec(28)].at = 6;
        kn[dec(28)].def = 4;
        kn[dec(28)].level = 7;
        kn[dec(28)].s = 12;
        kn[dec(28)].l = 12;
        kn[dec(28)].v = 16;

        kn[dec(29)].name = "Jho";
        kn[dec(29)].surname = "Gugi";
        kn[dec(29)].setTitle(COMMON);
        kn[dec(29)].setNation(OBERON_22);
        kn[dec(29)].town = "Alior";
        kn[dec(29)].at = 2;
        kn[dec(29)].def = 8;
        kn[dec(29)].level = 2;
        kn[dec(29)].s = 13;
        kn[dec(29)].l = 11;
        kn[dec(29)].v = 11;

        kn[dec(30)].name = "Dino";
        kn[dec(30)].surname = "Nensy";
        kn[dec(30)].setTitle(COMMON);
        kn[dec(30)].setNation(OBERON_22);
        kn[dec(30)].town = "Itejro";
        kn[dec(30)].at = 5;
        kn[dec(30)].def = 5;
        kn[dec(30)].level = 2;
        kn[dec(30)].s = 11;
        kn[dec(30)].l = 12;
        kn[dec(30)].v = 12;
    }

    public static void initDecks(Player[] kn) {
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
