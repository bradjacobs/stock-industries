# Stock Industry Sector Classification Parsing

## Description

For stock research & edification purposes, this project examines existing sector/industry classifications and renders them into a format that is more conveniently 'parsable' (such 
as JSON or CSV)

_UPDATE_: This project NOT kept up-to-date.

## Disclaimers
#### Part 1
Each of these classifications (or taxonomies) is owned by the respective companies listed below.  Please refer to their respective websites for specifics regarding trademarks 
and/or intellectual property rights.

|Abbreviation | Name/Website | Def'n Source | Def'n Format | 
| :--- | :--- | :--- | :--- |
| BICS<sup>(1)</sup> | [Bloomberg Industry Classification Standard](https://www.bloomberg.com/professional/product/reference-data/) | (N/A) | |
| CPC | [Central Product Classification](https://unstats.un.org/unsd/classifications/Econ/CPC.cshtml) | [source](https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt) | CSV |
| GICS | [Global Industry Classification Standard](https://www.msci.com/gics) (aka MSCI) | [source](https://www.msci.com/documents/1296102/11185224/GICS_map+2018.xlsx) | Excel |
| IBBICS<sup>(1)</sup> | [Industry Building Blocks Industry Classification System](http://industrybuildingblocks.com/) | (N/A) | |
| ICB | [Industry Classification Benchmark](https://www.ftserussell.com/data/industry-classification-benchmark-icb) | [source](https://content.ftserussell.com/sites/default/files/icb_structure_and_definitions.xlsx) | Excel |
| ISIC | [International Standard Industrial Classification](https://unstats.un.org/home/) | [source](http://www.ilo.org/ilostat-files/Documents/ISIC.xlsx) | Excel |
| MGECS | [Morningstar Global Equity Classification System](https://advisor.morningstar.com) | [source](https://advisor.morningstar.com/Enterprise/VTC/MorningstarGlobalEquityClassStructure2019v3.pdf) | PDF |
| NACE | [Statistical Classification of Economic Activities in the European Community](https://unstats.un.org/unsd/classifications) | [source](https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=LST_CLS_DLD&StrNom=NACE_REV2&StrLanguageCode=EN&StrLayoutCode=HIERARCHIC) | CSV |
| NAICS | [North American Industry Classification System](https://www.naics.com)<br>also see: [Census.gov/naics](https://www.census.gov/naics/) | [source](https://www.census.gov/naics/2022NAICS/2-6%20digit_2022_Codes.xlsx) | Excel |
| NAPCS | [North American Product Classification System](https://www.census.gov/naics/napcs/) | [source](https://www.census.gov/naics/napcs/structure/2022NAPCSStructure.xlsx) | Excel |
| RBICS<sup>(2)</sup> | [Revere Business Industry Classification System](https://www.sasb.org/find-your-industry/) (aka FactSet) | (N/A) | |
| SASB | [Sustainable Industry Classification System](https://www.sasb.org/find-your-industry/) | [source](https://www.sasb.org/find-your-industry/) | HTML |
| SIC | [Standard Industrial Classification](https://siccode.com) | [source](https://www.osha.gov/data/sic-manual) | HTML |
| SITC | [Standard International Trade Classification](https://unstats.un.org/unsd/classifications/Econ/) | [source](https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/SITCCodeandDescription.xlsx) | Excel |
| TRBC | [The Refinitiv Business Classification](https://www.refinitiv.com) | [source](https://www.refinitiv.com/content/dam/marketing/en_us/documents/quick-reference-guides/trbc-business-classification-quick-guide.pdf) | PDF |
| UNSPSC | [United Nations Standard Products and Services Code](https://www.ungm.org/Public/UNSPSC) | [source](https://www.ungm.org/Public/UNSPSC/Excel) | Excel |
| ZACKS | [Zacks Sector & Industry Classification](https://www.zacks.com) | [source](https://www.zacks.com/zrank/sector-industry-classification.php) <br>[alternate](http://www.zacksdata.com/app/download/247340904/Zacks+Sector+Industry+Mapping+Scheme.pdf) | HTML/PDF |

<sup>
(1) publicly available classification definitions not found<br>
(2) requires authorization to access data (see <a href="https://developer.factset.com/api-catalog/factset-rbics-api">here</a>)
</sup>

#### Part 2
* The intention is to keep all sector/industry definitions true and accurate to the original source.  However it's possible this project might reveal some subtle differences (like
 punctuation). If you require true reliability/accuracy of all sector/industry definitions, then please go to the respective source (links above)
* Links to the sources used are listed above, however cannot guarantee they are (or will remain) the most current version of any sector/industry definitions
