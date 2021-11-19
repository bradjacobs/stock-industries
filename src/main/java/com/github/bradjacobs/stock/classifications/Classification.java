package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.util.UrlUtil;

import java.net.MalformedURLException;
import java.net.URL;

public enum Classification
{
    //BICS("bics", ""),  // < - can't seen to find definition hierarchy spec.
    CPC("cpc", false,"https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt"),
    GICS("gics", true,"https://www.msci.com/documents/1296102/11185224/GICS_map+2018.xlsx"),
    ICB("icb", true,"https://content.ftserussell.com/sites/default/files/icb_structure_and_definitions.xlsx"),
    ISIC("isic", false,"http://www.ilo.org/ilostat-files/Documents/ISIC.xlsx"),
    MGECS("mgecs", true,"https://advisor.morningstar.com/Enterprise/VTC/MorningstarGlobalEquityClassStructure2019v3.pdf"),  // aka MorningStar
    NACE("nace", false, "https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=ACT_OTH_CLS_DLD&StrNom=NACE_REV2&StrFormat=CSV&StrLanguageCode=EN&IntKey=&IntLevel=&bExport="),
    NAICS("naics", true,"https://www.census.gov/naics/2017NAICS/2017_NAICS_Descriptions.xlsx"),
    NAPCS("napcs", false,"https://www.census.gov/naics/napcs/structure/2017NAPCSStructure.xlsx"),
    NASDAQ("nasdaq", false,"https://api.nasdaq.com/api/screener/stocks?limit=0&download=true"), // **
    //RBICS("rbics", false, ""), // requires authorization.  i.e.  https://developer.factset.com/api-catalog/factset-rbics-api
    SASB("sasb", true,"https://www.sasb.org/find-your-industry/"), // **
    SIC("sic", false,"https://www.osha.gov/data/sic-manual"),
    TRBC("trbc", false,"https://www.refinitiv.com/content/dam/marketing/en_us/documents/quick-reference-guides/trbc-business-classification-quick-guide.pdf"),  // aka Refinitiv
    ZACKS("zacks", false,"https://www.zacks.com/zrank/sector-industry-classification.php");

    private final String prefix;
    private final boolean longDescriptionAvailable;
    private final URL sourceFileLocation;

    Classification(String prefix, boolean longDescriptionAvailable, String sourceFileLocation)
    {
        this.prefix = prefix;
        this.longDescriptionAvailable = longDescriptionAvailable;
        this.sourceFileLocation = UrlUtil.createURL(sourceFileLocation);
    }

    public String getPrefix()
    {
        return prefix;
    }

    public boolean isLongDescriptionAvailable() {
        return longDescriptionAvailable;
    }

    public URL getSourceFileLocation()
    {
        return sourceFileLocation;
    }
}
