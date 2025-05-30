
/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;

class LocaleISOData {
    /**
     * ISO 639 2位和3位语言代码。
     */
    static final String isoLanguageTable =
          "aa" + "aar"  // Afar
        + "ab" + "abk"  // Abkhazian
        + "ae" + "ave"  // Avestan
        + "af" + "afr"  // Afrikaans
        + "ak" + "aka"  // Akan
        + "am" + "amh"  // Amharic
        + "an" + "arg"  // Aragonese
        + "ar" + "ara"  // Arabic
        + "as" + "asm"  // Assamese
        + "av" + "ava"  // Avaric
        + "ay" + "aym"  // Aymara
        + "az" + "aze"  // Azerbaijani
        + "ba" + "bak"  // Bashkir
        + "be" + "bel"  // Belarusian
        + "bg" + "bul"  // Bulgarian
        + "bh" + "bih"  // Bihari
        + "bi" + "bis"  // Bislama
        + "bm" + "bam"  // Bambara
        + "bn" + "ben"  // Bengali
        + "bo" + "bod"  // Tibetan
        + "br" + "bre"  // Breton
        + "bs" + "bos"  // Bosnian
        + "ca" + "cat"  // Catalan
        + "ce" + "che"  // Chechen
        + "ch" + "cha"  // Chamorro
        + "co" + "cos"  // Corsican
        + "cr" + "cre"  // Cree
        + "cs" + "ces"  // Czech
        + "cu" + "chu"  // Church Slavic
        + "cv" + "chv"  // Chuvash
        + "cy" + "cym"  // Welsh
        + "da" + "dan"  // Danish
        + "de" + "deu"  // German
        + "dv" + "div"  // Divehi
        + "dz" + "dzo"  // Dzongkha
        + "ee" + "ewe"  // Ewe
        + "el" + "ell"  // Greek
        + "en" + "eng"  // English
        + "eo" + "epo"  // Esperanto
        + "es" + "spa"  // Spanish
        + "et" + "est"  // Estonian
        + "eu" + "eus"  // Basque
        + "fa" + "fas"  // Persian
        + "ff" + "ful"  // Fulah
        + "fi" + "fin"  // Finnish
        + "fj" + "fij"  // Fijian
        + "fo" + "fao"  // Faroese
        + "fr" + "fra"  // French
        + "fy" + "fry"  // Frisian
        + "ga" + "gle"  // Irish
        + "gd" + "gla"  // Scottish Gaelic
        + "gl" + "glg"  // Gallegan
        + "gn" + "grn"  // Guarani
        + "gu" + "guj"  // Gujarati
        + "gv" + "glv"  // Manx
        + "ha" + "hau"  // Hausa
        + "he" + "heb"  // Hebrew
        + "hi" + "hin"  // Hindi
        + "ho" + "hmo"  // Hiri Motu
        + "hr" + "hrv"  // Croatian
        + "ht" + "hat"  // Haitian
        + "hu" + "hun"  // Hungarian
        + "hy" + "hye"  // Armenian
        + "hz" + "her"  // Herero
        + "ia" + "ina"  // Interlingua
        + "id" + "ind"  // Indonesian
        + "ie" + "ile"  // Interlingue
        + "ig" + "ibo"  // Igbo
        + "ii" + "iii"  // Sichuan Yi
        + "ik" + "ipk"  // Inupiaq
        + "in" + "ind"  // Indonesian (old)
        + "io" + "ido"  // Ido
        + "is" + "isl"  // Icelandic
        + "it" + "ita"  // Italian
        + "iu" + "iku"  // Inuktitut
        + "iw" + "heb"  // Hebrew (old)
        + "ja" + "jpn"  // Japanese
        + "ji" + "yid"  // Yiddish (old)
        + "jv" + "jav"  // Javanese
        + "ka" + "kat"  // Georgian
        + "kg" + "kon"  // Kongo
        + "ki" + "kik"  // Kikuyu
        + "kj" + "kua"  // Kwanyama
        + "kk" + "kaz"  // Kazakh
        + "kl" + "kal"  // Greenlandic
        + "km" + "khm"  // Khmer
        + "kn" + "kan"  // Kannada
        + "ko" + "kor"  // Korean
        + "kr" + "kau"  // Kanuri
        + "ks" + "kas"  // Kashmiri
        + "ku" + "kur"  // Kurdish
        + "kv" + "kom"  // Komi
        + "kw" + "cor"  // Cornish
        + "ky" + "kir"  // Kirghiz
        + "la" + "lat"  // Latin
        + "lb" + "ltz"  // Luxembourgish
        + "lg" + "lug"  // Ganda
        + "li" + "lim"  // Limburgish
        + "ln" + "lin"  // Lingala
        + "lo" + "lao"  // Lao
        + "lt" + "lit"  // Lithuanian
        + "lu" + "lub"  // Luba-Katanga
        + "lv" + "lav"  // Latvian
        + "mg" + "mlg"  // Malagasy
        + "mh" + "mah"  // Marshallese
        + "mi" + "mri"  // Maori
        + "mk" + "mkd"  // Macedonian
        + "ml" + "mal"  // Malayalam
        + "mn" + "mon"  // Mongolian
        + "mo" + "mol"  // Moldavian
        + "mr" + "mar"  // Marathi
        + "ms" + "msa"  // Malay
        + "mt" + "mlt"  // Maltese
        + "my" + "mya"  // Burmese
        + "na" + "nau"  // Nauru
        + "nb" + "nob"  // Norwegian Bokm?l
        + "nd" + "nde"  // North Ndebele
        + "ne" + "nep"  // Nepali
        + "ng" + "ndo"  // Ndonga
        + "nl" + "nld"  // Dutch
        + "nn" + "nno"  // Norwegian Nynorsk
        + "no" + "nor"  // Norwegian
        + "nr" + "nbl"  // South Ndebele
        + "nv" + "nav"  // Navajo
        + "ny" + "nya"  // Nyanja
        + "oc" + "oci"  // Occitan
        + "oj" + "oji"  // Ojibwa
        + "om" + "orm"  // Oromo
        + "or" + "ori"  // Oriya
        + "os" + "oss"  // Ossetian
        + "pa" + "pan"  // Panjabi
        + "pi" + "pli"  // Pali
        + "pl" + "pol"  // Polish
        + "ps" + "pus"  // Pushto
        + "pt" + "por"  // Portuguese
        + "qu" + "que"  // Quechua
        + "rm" + "roh"  // Raeto-Romance
        + "rn" + "run"  // Rundi
        + "ro" + "ron"  // Romanian
        + "ru" + "rus"  // Russian
        + "rw" + "kin"  // Kinyarwanda
        + "sa" + "san"  // Sanskrit
        + "sc" + "srd"  // Sardinian
        + "sd" + "snd"  // Sindhi
        + "se" + "sme"  // Northern Sami
        + "sg" + "sag"  // Sango
        + "si" + "sin"  // Sinhalese
        + "sk" + "slk"  // Slovak
        + "sl" + "slv"  // Slovenian
        + "sm" + "smo"  // Samoan
        + "sn" + "sna"  // Shona
        + "so" + "som"  // Somali
        + "sq" + "sqi"  // Albanian
        + "sr" + "srp"  // Serbian
        + "ss" + "ssw"  // Swati
        + "st" + "sot"  // Southern Sotho
        + "su" + "sun"  // Sundanese
        + "sv" + "swe"  // Swedish
        + "sw" + "swa"  // Swahili
        + "ta" + "tam"  // Tamil
        + "te" + "tel"  // Telugu
        + "tg" + "tgk"  // Tajik
        + "th" + "tha"  // Thai
        + "ti" + "tir"  // Tigrinya
        + "tk" + "tuk"  // Turkmen
        + "tl" + "tgl"  // Tagalog
        + "tn" + "tsn"  // Tswana
        + "to" + "ton"  // Tonga
        + "tr" + "tur"  // Turkish
        + "ts" + "tso"  // Tsonga
        + "tt" + "tat"  // Tatar
        + "tw" + "twi"  // Twi
        + "ty" + "tah"  // Tahitian
        + "ug" + "uig"  // Uighur
        + "uk" + "ukr"  // Ukrainian
        + "ur" + "urd"  // Urdu
        + "uz" + "uzb"  // Uzbek
        + "ve" + "ven"  // Venda
        + "vi" + "vie"  // Vietnamese
        + "vo" + "vol"  // Volap?k
        + "wa" + "wln"  // Walloon
        + "wo" + "wol"  // Wolof
        + "xh" + "xho"  // Xhosa
        + "yi" + "yid"  // Yiddish
        + "yo" + "yor"  // Yoruba
        + "za" + "zha"  // Zhuang
        + "zh" + "zho"  // Chinese
        + "zu" + "zul"  // Zulu
        ;


/**
 * ISO 3166 的 2 位和 3 位国家代码。
 */
static final String isoCountryTable =
      "AD" + "AND"  // 安道尔，安道尔公国
    + "AE" + "ARE"  // 阿拉伯联合酋长国
    + "AF" + "AFG"  // 阿富汗
    + "AG" + "ATG"  // 安提瓜和巴布达
    + "AI" + "AIA"  // 安圭拉
    + "AL" + "ALB"  // 阿尔巴尼亚，阿尔巴尼亚人民社会主义共和国
    + "AM" + "ARM"  // 亚美尼亚
    + "AN" + "ANT"  // 荷兰安的列斯群岛
    + "AO" + "AGO"  // 安哥拉，安哥拉共和国
    + "AQ" + "ATA"  // 南极洲（南纬 60 度以南的地区）
    + "AR" + "ARG"  // 阿根廷，阿根廷共和国
    + "AS" + "ASM"  // 美属萨摩亚
    + "AT" + "AUT"  // 奥地利，奥地利共和国
    + "AU" + "AUS"  // 澳大利亚，澳大利亚联邦
    + "AW" + "ABW"  // 阿鲁巴
    + "AX" + "ALA"  // 奥兰群岛
    + "AZ" + "AZE"  // 阿塞拜疆，阿塞拜疆共和国
    + "BA" + "BIH"  // 波斯尼亚和黑塞哥维那
    + "BB" + "BRB"  // 巴巴多斯
    + "BD" + "BGD"  // 孟加拉国，孟加拉人民共和国
    + "BE" + "BEL"  // 比利时，比利时王国
    + "BF" + "BFA"  // 布基纳法索
    + "BG" + "BGR"  // 保加利亚，保加利亚人民共和国
    + "BH" + "BHR"  // 巴林，巴林王国
    + "BI" + "BDI"  // 布隆迪，布隆迪共和国
    + "BJ" + "BEN"  // 贝宁，贝宁人民共和国
    + "BL" + "BLM"  // 圣巴泰勒米
    + "BM" + "BMU"  // 百慕大
    + "BN" + "BRN"  // 文莱达鲁萨兰国
    + "BO" + "BOL"  // 玻利维亚，玻利维亚共和国
    + "BQ" + "BES"  // 波内赫尔、圣尤斯特歇斯和萨巴
    + "BR" + "BRA"  // 巴西，巴西联邦共和国
    + "BS" + "BHS"  // 巴哈马，巴哈马联邦
    + "BT" + "BTN"  // 不丹，不丹王国
    + "BV" + "BVT"  // 布维岛（布维岛）
    + "BW" + "BWA"  // 博茨瓦纳，博茨瓦纳共和国
    + "BY" + "BLR"  // 白俄罗斯
    + "BZ" + "BLZ"  // 伯利兹
    + "CA" + "CAN"  // 加拿大
    + "CC" + "CCK"  // 科科斯（基林）群岛
    + "CD" + "COD"  // 刚果民主共和国
    + "CF" + "CAF"  // 中非共和国
    + "CG" + "COG"  // 刚果，刚果人民共和国
    + "CH" + "CHE"  // 瑞士，瑞士联邦
    + "CI" + "CIV"  // 科特迪瓦，象牙海岸，科特迪瓦共和国
    + "CK" + "COK"  // 库克群岛
    + "CL" + "CHL"  // 智利，智利共和国
    + "CM" + "CMR"  // 喀麦隆，喀麦隆联合共和国
    + "CN" + "CHN"  // 中国，中华人民共和国
    + "CO" + "COL"  // 哥伦比亚，哥伦比亚共和国
    + "CR" + "CRI"  // 哥斯达黎加，哥斯达黎加共和国
//  + "CS" + "SCG"  // 塞尔维亚和黑山
    + "CU" + "CUB"  // 古巴，古巴共和国
    + "CV" + "CPV"  // 佛得角，佛得角共和国
    + "CW" + "CUW"  // 库拉索
    + "CX" + "CXR"  // 圣诞岛
    + "CY" + "CYP"  // 塞浦路斯，塞浦路斯共和国
    + "CZ" + "CZE"  // 捷克共和国
    + "DE" + "DEU"  // 德国
    + "DJ" + "DJI"  // 吉布提，吉布提共和国
    + "DK" + "DNK"  // 丹麦，丹麦王国
    + "DM" + "DMA"  // 多米尼克，多米尼克联邦
    + "DO" + "DOM"  // 多米尼加共和国
    + "DZ" + "DZA"  // 阿尔及利亚，阿尔及利亚人民民主共和国
    + "EC" + "ECU"  // 厄瓜多尔，厄瓜多尔共和国
    + "EE" + "EST"  // 爱沙尼亚
    + "EG" + "EGY"  // 埃及，阿拉伯埃及共和国
    + "EH" + "ESH"  // 西撒哈拉
    + "ER" + "ERI"  // 厄立特里亚
    + "ES" + "ESP"  // 西班牙，西班牙国
    + "ET" + "ETH"  // 埃塞俄比亚
    + "FI" + "FIN"  // 芬兰，芬兰共和国
    + "FJ" + "FJI"  // 斐济，斐济群岛共和国
    + "FK" + "FLK"  // 福克兰群岛（马尔维纳斯群岛）
    + "FM" + "FSM"  // 密克罗尼西亚，密克罗尼西亚联邦
    + "FO" + "FRO"  // 法罗群岛
    + "FR" + "FRA"  // 法国，法兰西共和国
    + "GA" + "GAB"  // 加蓬，加蓬共和国
    + "GB" + "GBR"  // 英国，大不列颠及北爱尔兰联合王国
    + "GD" + "GRD"  // 格林纳达
    + "GE" + "GEO"  // 格鲁吉亚
    + "GF" + "GUF"  // 法属圭亚那
    + "GG" + "GGY"  // 根西岛
    + "GH" + "GHA"  // 加纳，加纳共和国
    + "GI" + "GIB"  // 直布罗陀
    + "GL" + "GRL"  // 格陵兰
    + "GM" + "GMB"  // 冈比亚，冈比亚共和国
    + "GN" + "GIN"  // 几内亚，几内亚革命人民共和国
    + "GP" + "GLP"  // 瓜德罗普
    + "GQ" + "GNQ"  // 赤道几内亚，赤道几内亚共和国
    + "GR" + "GRC"  // 希腊，希腊共和国
    + "GS" + "SGS"  // 南乔治亚岛和南桑威奇群岛
    + "GT" + "GTM"  // 危地马拉，危地马拉共和国
    + "GU" + "GUM"  // 关岛
    + "GW" + "GNB"  // 几内亚比绍，几内亚比绍共和国
    + "GY" + "GUY"  // 圭亚那，圭亚那共和国
    + "HK" + "HKG"  // 中国香港特别行政区
    + "HM" + "HMD"  // 赫德岛和麦克唐纳群岛
    + "HN" + "HND"  // 洪都拉斯，洪都拉斯共和国
    + "HR" + "HRV"  // 克罗地亚（克罗地亚）
    + "HT" + "HTI"  // 海地，海地共和国
    + "HU" + "HUN"  // 匈牙利，匈牙利人民共和国
    + "ID" + "IDN"  // 印度尼西亚，印度尼西亚共和国
    + "IE" + "IRL"  // 爱尔兰
    + "IL" + "ISR"  // 以色列，以色列国
    + "IM" + "IMN"  // 曼岛
    + "IN" + "IND"  // 印度，印度共和国
    + "IO" + "IOT"  // 英属印度洋领地（查戈斯群岛）
    + "IQ" + "IRQ"  // 伊拉克，伊拉克共和国
    + "IR" + "IRN"  // 伊朗，伊朗伊斯兰共和国
    + "IS" + "ISL"  // 冰岛，冰岛共和国
    + "IT" + "ITA"  // 意大利，意大利共和国
    + "JE" + "JEY"  // 泽西岛
    + "JM" + "JAM"  // 牙买加
    + "JO" + "JOR"  // 约旦，约旦哈希姆王国
    + "JP" + "JPN"  // 日本
    + "KE" + "KEN"  // 肯尼亚，肯尼亚共和国
    + "KG" + "KGZ"  // 吉尔吉斯斯坦
    + "KH" + "KHM"  // 柬埔寨，柬埔寨王国
    + "KI" + "KIR"  // 基里巴斯，基里巴斯共和国
    + "KM" + "COM"  // 科摩罗，科摩罗联盟
    + "KN" + "KNA"  // 圣基茨和尼维斯
    + "KP" + "PRK"  // 朝鲜，朝鲜民主主义人民共和国
    + "KR" + "KOR"  // 韩国，大韩民国
    + "KW" + "KWT"  // 科威特，科威特国
    + "KY" + "CYM"  // 开曼群岛
    + "KZ" + "KAZ"  // 哈萨克斯坦，哈萨克斯坦共和国
    + "LA" + "LAO"  // 老挝人民民主共和国
    + "LB" + "LBN"  // 黎巴嫩，黎巴嫩共和国
    + "LC" + "LCA"  // 圣卢西亚
    + "LI" + "LIE"  // 列支敦士登，列支敦士登公国
    + "LK" + "LKA"  // 斯里兰卡，斯里兰卡民主社会主义共和国
    + "LR" + "LBR"  // 利比里亚，利比里亚共和国
    + "LS" + "LSO"  // 莱索托，莱索托王国
    + "LT" + "LTU"  // 立陶宛
    + "LU" + "LUX"  // 卢森堡，卢森堡大公国
    + "LV" + "LVA"  // 拉脱维亚
    + "LY" + "LBY"  // 利比亚阿拉伯民众国
    + "MA" + "MAR"  // 摩洛哥，摩洛哥王国
    + "MC" + "MCO"  // 摩纳哥，摩纳哥公国
    + "MD" + "MDA"  // 摩尔多瓦，摩尔多瓦共和国
    + "ME" + "MNE"  // 黑山，黑山共和国
    + "MF" + "MAF"  // 圣马丁
    + "MG" + "MDG"  // 马达加斯加，马达加斯加共和国
    + "MH" + "MHL"  // 马绍尔群岛
    + "MK" + "MKD"  // 马其顿，前南斯拉夫马其顿共和国
    + "ML" + "MLI"  // 马里，马里共和国
    + "MM" + "MMR"  // 缅甸
    + "MN" + "MNG"  // 蒙古，蒙古人民共和国
    + "MO" + "MAC"  // 澳门，中国澳门特别行政区
    + "MP" + "MNP"  // 北马里亚纳群岛
    + "MQ" + "MTQ"  // 马提尼克
    + "MR" + "MRT"  // 毛里塔尼亚，毛里塔尼亚伊斯兰共和国
    + "MS" + "MSR"  // 蒙特塞拉特
    + "MT" + "MLT"  // 马耳他，马耳他共和国
    + "MU" + "MUS"  // 毛里求斯
    + "MV" + "MDV"  // 马尔代夫，马尔代夫共和国
    + "MW" + "MWI"  // 马拉维，马拉维共和国
    + "MX" + "MEX"  // 墨西哥，墨西哥合众国
    + "MY" + "MYS"  // 马来西亚
    + "MZ" + "MOZ"  // 莫桑比克，莫桑比克人民共和国
    + "NA" + "NAM"  // 纳米比亚
    + "NC" + "NCL"  // 新喀里多尼亚
    + "NE" + "NER"  // 尼日尔，尼日尔共和国
    + "NF" + "NFK"  // 诺福克岛
    + "NG" + "NGA"  // 尼日利亚，尼日利亚联邦共和国
    + "NI" + "NIC"  // 尼加拉瓜，尼加拉瓜共和国
    + "NL" + "NLD"  // 荷兰，荷兰王国
    + "NO" + "NOR"  // 挪威，挪威王国
    + "NP" + "NPL"  // 尼泊尔，尼泊尔王国
    + "NR" + "NRU"  // 瑙鲁，瑙鲁共和国
    + "NU" + "NIU"  // 努伊，努伊共和国
    + "NZ" + "NZL"  // 新西兰
    + "OM" + "OMN"  // 阿曼，阿曼苏丹国
    + "PA" + "PAN"  // 巴拿马，巴拿马共和国
    + "PE" + "PER"  // 秘鲁，秘鲁共和国
    + "PF" + "PYF"  // 法属波利尼西亚
    + "PG" + "PNG"  // 巴布亚新几内亚
    + "PH" + "PHL"  // 菲律宾，菲律宾共和国
    + "PK" + "PAK"  // 巴基斯坦，巴基斯坦伊斯兰共和国
    + "PL" + "POL"  // 波兰，波兰共和国
    + "PM" + "SPM"  // 圣皮埃尔和密克隆
    + "PN" + "PCN"  // 皮特凯恩岛
    + "PR" + "PRI"  // 波多黎各
    + "PS" + "PSE"  // 巴勒斯坦领土，被占领
    + "PT" + "PRT"  // 葡萄牙，葡萄牙共和国
    + "PW" + "PLW"  // 帕劳
    + "PY" + "PRY"  // 巴拉圭，巴拉圭共和国
    + "QA" + "QAT"  // 卡塔尔，卡塔尔国
    + "RE" + "REU"  // 留尼汪
    + "RO" + "ROU"  // 罗马尼亚，罗马尼亚社会主义共和国
    + "RS" + "SRB"  // 塞尔维亚，塞尔维亚共和国
    + "RU" + "RUS"  // 俄罗斯联邦
    + "RW" + "RWA"  // 卢旺达，卢旺达共和国
    + "SA" + "SAU"  // 沙特阿拉伯，沙特阿拉伯王国
    + "SB" + "SLB"  // 所罗门群岛
    + "SC" + "SYC"  // 塞舌尔，塞舌尔共和国
    + "SD" + "SDN"  // 苏丹，苏丹民主共和国
    + "SE" + "SWE"  // 瑞典，瑞典王国
    + "SG" + "SGP"  // 新加坡，新加坡共和国
    + "SH" + "SHN"  // 圣赫勒拿
    + "SI" + "SVN"  // 斯洛文尼亚
    + "SJ" + "SJM"  // 斯瓦尔巴群岛和扬马延群岛
    + "SK" + "SVK"  // 斯洛伐克（斯洛伐克共和国）
    + "SL" + "SLE"  // 塞拉利昂，塞拉利昂共和国
    + "SM" + "SMR"  // 圣马力诺，圣马力诺共和国
    + "SN" + "SEN"  // 塞内加尔，塞内加尔共和国
    + "SO" + "SOM"  // 索马里，索马里共和国
    + "SR" + "SUR"  // 苏里南，苏里南共和国
    + "SS" + "SSD"  // 南苏丹
    + "ST" + "STP"  // 圣多美和普林西比，圣多美和普林西比民主共和国
    + "SV" + "SLV"  // 萨尔瓦多，萨尔瓦多共和国
    + "SX" + "SXM"  // 圣马丁（荷兰部分）
    + "SY" + "SYR"  // 叙利亚阿拉伯共和国
    + "SZ" + "SWZ"  // 斯威士兰，斯威士兰王国
    + "TC" + "TCA"  // 特克斯和凯科斯群岛
    + "TD" + "TCD"  //乍得，乍得共和国
    + "TF" + "ATF"  // 法属南部领地
    + "TG" + "TGO"  // 多哥，多哥共和国
    + "TH" + "THA"  // 泰国，泰国王国
    + "TJ" + "TJK"  // 塔吉克斯坦
    + "TK" + "TKL"  // 托克劳（托克劳群岛）
    + "TL" + "TLS"  // 东帝汶，东帝汶民主共和国
    + "TM" + "TKM"  // 土库曼斯坦
    + "TN" + "TUN"  // 突尼斯，突尼斯共和国
    + "TO" + "TON"  // 汤加，汤加王国
    + "TR" + "TUR"  // 土耳其，土耳其共和国
    + "TT" + "TTO"  // 特立尼达和多巴哥，特立尼达和多巴哥共和国
    + "TV" + "TUV"  // 图瓦卢
    + "TW" + "TWN"  // 中国台湾省
    + "TZ" + "TZA"  // 坦桑尼亚，坦桑尼亚联合共和国
    + "UA" + "UKR"  // 乌克兰
    + "UG" + "UGA"  // 乌干达，乌干达共和国
    + "UM" + "UMI"  // 美国本土外小岛屿
    + "US" + "USA"  // 美利坚合众国
    + "UY" + "URY"  // 乌拉圭，乌拉圭东岸共和国
    + "UZ" + "UZB"  // 乌兹别克斯坦
    + "VA" + "VAT"  // 梵蒂冈（梵蒂冈城国）
    + "VC" + "VCT"  // 圣文森特和格林纳丁斯
    + "VE" + "VEN"  // 委内瑞拉，委内瑞拉玻利瓦尔共和国
    + "VG" + "VGB"  // 英属维尔京群岛
    + "VI" + "VIR"  // 美属维尔京群岛
    + "VN" + "VNM"  // 越南，越南社会主义共和国
    + "VU" + "VUT"  // 瓦努阿图
    + "WF" + "WLF"  // 瓦利斯和富图纳群岛
    + "WS" + "WSM"  // 萨摩亚，萨摩亚独立国
    + "YE" + "YEM"  // 也门
    + "YT" + "MYT"  // 马约特
    + "ZA" + "ZAF"  // 南非，南非共和国
    + "ZM" + "ZMB"  // 赞比亚，赞比亚共和国
    + "ZW" + "ZWE"  // 津巴布韦
    ;

private LocaleISOData() {
}
