// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:38 PST by lamport
//      modified on Fri Jun 18 18:13:00 PDT 1999 by yuanyu

package tlc2.util;

import java.util.Random;

public class RandomGenerator extends Random {

  private static final long serialVersionUID = 1180606528797062350L;

private long aril;

  public final static int[] primes =
  { 1277011, 1277021, 1277039, 1277041, 1277063, 1277069, 1277071, 1277083, 1277093,
    1277099, 1277113, 1277137, 1277147, 1277197, 1277207, 1277209, 1277233, 1277249,
    1277257, 1277267, 1277299, 1277321, 1277323, 1277357, 1277359, 1277369, 1277387,
    1277429, 1277449, 1277461, 1277477, 1277483, 1277491, 1277501, 1277543, 1277557,
    1277569, 1277593, 1277597, 1277621, 1277629, 1277651, 1277657, 1277677, 1277699,
    1277723, 1277729, 1277741, 1277743, 1277753, 1277761, 1277791, 1277803, 1277813,
    1277819, 1277833, 1277849, 1277863, 1277867, 1277879, 1277897, 1277909, 1277911,
    1277957, 1277971, 1277993, 1278007, 1278029, 1278031, 1278047, 1278097, 1278107,
    1278113, 1278131, 1278139, 1278163, 1278181, 1278191, 1278197, 1278203, 1278209,
    1278217, 1278227, 1278253, 1278287, 1278289, 1278323, 1278337, 1278341, 1278371,
    1278373, 1278379, 1278391, 1278397, 1278401, 1278419, 1278437, 1278439, 1278463,
    1278467, 1278479, 1278481, 1278493, 1278527, 1278551, 1278583, 1278601, 1278611,
    1278617, 1278619, 1278623, 1278631, 1278637, 1278659, 1278671, 1278701, 1278709,
    1278713, 1278721, 1278733, 1278769, 1278779, 1278787, 1278799, 1278803, 1278811,
    1278817, 1278839, 1278857, 1278881, 1278899, 1278911, 1278983, 1278997, 1279001,
    1279013, 1279021, 1279027, 1279039, 1279043, 1279081, 1279087, 1279093, 1279111,
    1279123, 1279133, 1279141, 1279163, 1279171, 1279177, 1279181, 1279183, 1279189,
    1279193, 1279211, 1279249, 1279253, 1279303, 1279307, 1279309, 1279319, 1279321,
    1279337, 1279357, 1279361, 1279417, 1279427, 1279457, 1279459, 1279483, 1279493,
    1279507, 1279511, 1279519, 1279541, 1279547, 1279549, 1279561, 1279583, 1279601,
    1279609, 1279627, 1279643, 1279657, 1279661, 1279667, 1279673, 1279679, 1279687,
    1279693, 1279703, 1279727, 1279753, 1279757, 1279787, 1279801, 1279807, 1279813,
    1279819, 1279823, 1279843, 1279847, 1279853, 1279871, 1279877, 1279907, 1279919,
    1279921, 1279931, 1279937, 1279961, 1279969, 1279997, 1280023, 1280101, 1280107,
    1280113, 1280119, 1280129, 1280131, 1280141, 1280159, 1280161, 1280173, 1280179,
    1280183, 1280221, 1280231, 1280267, 1280281, 1280291, 1280297, 1280309, 1280317,
    1280333, 1280371, 1280399, 1280401, 1280407, 1280417, 1280431, 1280453, 1280473,
    1280519, 1280537, 1280549, 1280561, 1280567, 1280597, 1280603, 1280623, 1280633,
    1280651, 1280659, 1280677, 1280693, 1280707, 1280737, 1280743, 1280759, 1280761,
    1280767, 1280789, 1280791, 1280803, 1280821, 1280833, 1280837, 1280857, 1280863,
    1280869, 1280887, 1280921, 1280947, 1280969, 1280987, 1280989, 1281029, 1281041,
    1281043, 1281047, 1281083, 1281089, 1281097, 1281101, 1281131, 1281149, 1281157,
    1281167, 1281187, 1281193, 1281211, 1281221, 1281229, 1281253, 1281257, 1281263,
    1281281, 1281283, 1281317, 1281331, 1281349, 1281367, 1281383, 1281389, 1281407,
    1281431, 1281433, 1281439, 1281451, 1281457, 1281463, 1281503, 1281521, 1281523,
    1281541, 1281547, 1281551, 1281563, 1281587, 1281649, 1281653, 1281667, 1281673,
    1281677, 1281691, 1281697, 1281703, 1281727, 1281739, 1281751, 1281773, 1281779,
    1281781, 1281799, 1281803, 1281809, 1281821, 1281823, 1281827, 1281853, 1281871,
    1281883, 1281899, 1281937, 1281941, 1281961, 1281971, 1281979, 1281983, 1282007,
    1282009, 1282031, 1282033, 1282051, 1282069, 1282079, 1282081, 1282093, 1282109,
    1282117, 1282121, 1282133, 1282153, 1282163, 1282187, 1282201, 1282213, 1282231,
    1282241, 1282261, 1282277, 1282279, 1282289, 1282297, 1282343, 1282349, 1282363,
    1282381, 1282387, 1282399, 1282417, 1282423, 1282427, 1282451, 1282469, 1282471,
    1282493, 1282499, 1282507, 1282511, 1282513, 1282517, 1282529, 1282543, 1282571,
    1282577, 1282597, 1282607, 1282613, 1282627, 1282637, 1282639, 1282649, 1282657,
    1282661, 1282681, 1282693, 1282703, 1282717, 1282739, 1282751, 1282763, 1282781,
    1282783, 1282807, 1282817, 1282867, 1282877, 1282903, 1282907, 1282909, 1282913,
    1282933, 1282943, 1282951, 1282961, 1282969, 1282993, 1283011, 1283017, 1283021,
    1283027, 1283063, 1283069, 1283083, 1283099, 1283111, 1283119, 1283129, 1283137,
    1283159, 1283167, 1283171, 1283173, 1283179, 1283207, 1283237, 1283297, 1283323,
    1283333, 1283339, 1283353, 1283383, 1283389, 1283417, 1283437, 1283441, 1283473,
    1283479, 1283509, 1283521, 1283537, 1283539, 1283543, 1283549, 1283563, 1283573,
    1283591, 1283603, 1283677, 1283683, 1283701, 1283707, 1283717, 1283719, 1283731,
    1283753, 1283759, 1283767, 1283771, 1283797, 1283831, 1283839, 1283873, 1283879,
    1283881, 1283897, 1283903, 1283939, 1283941, 1283957, 1283969, 1283981, 1283983,
    1284007, 1284037, 1284043, 1284047, 1284053, 1284083, 1284131, 1284169, 1284187,
    1284209, 1284211, 1284223, 1284263, 1284271, 1284287, 1284293, 1284301, 1284313,
    1284317, 1284329, 1284341, 1284373, 1284379, 1284383, 1284421, 1284427, 1284433,
    1284443, 1284467, 1284473, 1284487, 1284511, 1284523, 1284541, 1284551, 1284553,
    1284559, 1284583, 1284601, 1284617, 1284623, 1284631, 1284641, 1284659, 1284691,
    1284709, 1284713, 1284737, 1284739, 1284763, 1284769, 1284791, 1284793, 1284823,
    1284841, 1284847, 1284851, 1284863, 1284889, 1284901, 1284917, 1284931, 1284937,
    1284967, 1284971, 1284977, 1284991, 1285021, 1285049, 1285051, 1285057, 1285061,
    1285069, 1285099, 1285111, 1285117, 1285129, 1285139, 1285147, 1285159, 1285169,
    1285181, 1285199, 1285213, 1285223, 1285231, 1285237, 1285247, 1285259, 1285267,
    1285279, 1285283, 1285289, 1285301, 1285351, 1285381, 1285393, 1285397, 1285411,
    1285429, 1285441, 1285451, 1285469, 1285481, 1285507, 1285511, 1285513, 1285517,
    1285519, 1285547, 1285549, 1285553, 1285607, 1285619, 1285633, 1285649, 1285679,
    1285699, 1285703, 1285717, 1285741, 1285747, 1285759, 1285763, 1285777, 1285789,
    1285793, 1285799, 1285811, 1285813, 1285841, 1285847, 1285853, 1285859, 1285871,
    1285877, 1285891, 1285903, 1285913, 1285937, 1285943, 1285969, 1285981, 1285993,
    1286011, 1286017, 1286039, 1286071, 1286081, 1286093, 1286099, 1286107, 1286119,
    1286147, 1286149, 1286177, 1286189, 1286191, 1286209, 1286227, 1286239, 1286261,
    1286267, 1286269, 1286273, 1286287, 1286303, 1286323, 1286359, 1286371, 1286381,
    1286387, 1286399, 1286419, 1286447, 1286489, 1286491, 1286503, 1286513, 1286521,
    1286533, 1286557, 1286561, 1286569, 1286581, 1286587, 1286617, 1286629, 1286633,
    1286641, 1286647, 1286653, 1286657, 1286669, 1286683, 1286693, 1286707, 1286711,
    1286773, 1286777, 1286783, 1286797, 1286807, 1286819, 1286821, 1286833, 1286837,
    1286839, 1286843, 1286881, 1286939, 1286941, 1286953, 1286959, 1286969, 1286981,
    1286983, 1287007, 1287047, 1287059, 1287061, 1287067, 1287071, 1287101, 1287109,
    1287131, 1287133, 1287157, 1287163, 1287173, 1287179, 1287197, 1287199, 1287217,
    1287233, 1287239, 1287289, 1287323, 1287329, 1287343, 1287347, 1287353, 1287361,
    1287371, 1287373, 1287401, 1287431, 1287457, 1287467, 1287469, 1287479, 1287487,
    1287491, 1287499, 1287511, 1287541, 1287551, 1287553, 1287569, 1287589, 1287593,
    1287607, 1287613, 1287623, 1287661, 1287683, 1287691, 1287697, 1287707, 1287731,
    1287739, 1287743, 1287749, 1287751, 1287757, 1287761, 1287787, 1287799, 1287817,
    1287821, 1287829, 1287841, 1287857, 1287883, 1287887, 1287899, 1287917, 1287947,
    1287961, 1287967, 1287973, 1287983, 1287989, 1287997, 1288003, 1288009, 1288013,
    1288033, 1288037, 1288043, 1288051, 1288057, 1288061, 1288099, 1288103, 1288109,
    1288117, 1288163, 1288169, 1288171, 1288187, 1288193, 1288201, 1288213, 1288247,
    1288249, 1288291, 1288307, 1288337, 1288349, 1288361, 1288363, 1288367, 1288393,
    1288421, 1288423, 1288429, 1288439, 1288487, 1288513, 1288519, 1288531, 1288541,
    1288543, 1288559, 1288571, 1288597, 1288603, 1288607, 1288613, 1288643, 1288649,
    1288657, 1288691, 1288697, 1288699, 1288709, 1288711, 1288733, 1288769, 1288783,
    1288799, 1288817, 1288823, 1288829, 1288831, 1288843, 1288849, 1288853, 1288871,
    1288873, 1288877, 1288891, 1288919, 1288921, 1288933, 1288939, 1288951, 1288967,
    1288981, 1288993, 1288997, 1289003, 1289009, 1289027, 1289039, 1289053, 1289077,
    1289083, 1289111, 1289129, 1289149, 1289153, 1289159, 1289179, 1289213, 1289231,
    1289237, 1289261, 1289273, 1289287, 1289303, 1289329, 1289333, 1289341, 1289363,
    1289371, 1289381, 1289401, 1289411, 1289423, 1289429, 1289447, 1289459, 1289513,
    1289531, 1289537, 1289551, 1289557, 1289567, 1289593, 1289597, 1289599, 1289621,
    1289623, 1289627, 1289653, 1289657, 1289677, 1289711, 1289713, 1289731, 1289747,
    1289749, 1289753, 1289779, 1289789, 1289801, 1289803, 1289831, 1289839, 1289851,
    1289867, 1289881, 1289921, 1289927, 1289933, 1289963, 1289969, 1289971, 1290013,
    1290019, 1290031, 1290049, 1290077, 1290083, 1290109, 1290131, 1290143, 1290151,
    1290161, 1290167, 1290169, 1290173, 1290199, 1290203, 1290209, 1290257, 1290259,
    1290287, 1290293, 1290299, 1290319, 1290329, 1290371, 1290379, 1290427, 1290431,
    1290433, 1290439, 1290463, 1290467, 1290469, 1290491, 1290503, 1290533, 1290539,
    1290551, 1290563, 1290571, 1290581, 1290593, 1290607, 1290629, 1290631, 1290637,
    1290643, 1290649, 1290659, 1290673, 1290683, 1290719, 1290791, 1290811, 1290823,
    1290847, 1290853, 1290857, 1290869, 1290901, 1290907, 1290923, 1290937, 1290983,
    1291001, 1291007, 1291009, 1291019, 1291021, 1291063, 1291079, 1291111, 1291117,
    1291139, 1291153, 1291159, 1291163, 1291177, 1291193, 1291211, 1291217, 1291219,
    1291223, 1291229, 1291249, 1291271, 1291313, 1291321, 1291327, 1291343, 1291349,
    1291357, 1291369, 1291379, 1291387, 1291391, 1291421, 1291447, 1291453, 1291471,
    1291481, 1291483, 1291489, 1291501, 1291523, 1291547, 1291567, 1291579, 1291603,
    1291637, 1291669, 1291673, 1291691, 1291783, 1291793, 1291799, 1291817, 1291819,
    1291831, 1291861, 1291877, 1291883, 1291907, 1291909, 1291931, 1291957, 1291963,
    1291967, 1291991, 1291999, 1292009, 1292023, 1292029, 1292063, 1292069, 1292089,
    1292099, 1292113, 1292131, 1292141, 1292143, 1292149, 1292167, 1292177, 1292219,
    1292237, 1292243, 1292251, 1292257, 1292261, 1292281, 1292293, 1292309, 1292329,
    1292339, 1292353, 1292371, 1292383, 1292387, 1292419, 1292429, 1292441, 1292477,
    1292491, 1292503, 1292509, 1292539, 1292549, 1292563, 1292567, 1292579, 1292587,
    1292591, 1292593, 1292597, 1292609, 1292633, 1292639, 1292653, 1292657, 1292659,
    1292693, 1292701, 1292713, 1292717, 1292729, 1292737, 1292783, 1292789, 1292801,
    1292813, 1292831, 1292843, 1292857, 1292887, 1292927, 1292947, 1292953, 1292957,
    1292971, 1292983, 1292989, 1292999, 1293001, 1293011, 1293031, 1293077, 1293119,
    1293133, 1293137, 1293157, 1293169, 1293179, 1293199, 1293203, 1293233, 1293239,
    1293247, 1293251, 1293277, 1293283, 1293287, 1293307, 1293317, 1293319, 1293323,
    1293329, 1293361, 1293367, 1293373, 1293401, 1293419, 1293421, 1293433, 1293473,
    1293491, 1293493, 1293499, 1293529, 1293533, 1293541, 1293553, 1293559, 1293583,
    1293587, 1293613, 1293619, 1293647, 1293659, 1293701, 1293739, 1293757, 1293763,
    1293791, 1293797, 1293821, 1293829, 1293839, 1293841, 1293857, 1293869, 1293899,
    1293917, 1293923, 1293931, 1293947, 1293949, 1293961, 1293967, 1293977, 1293979,
    1293983, 1294019, 1294021, 1294031, 1294037, 1294039, 1294061, 1294081, 1294087,
    1294103, 1294121, 1294123, 1294129, 1294169, 1294177, 1294199, 1294201, 1294231,
    1294253, 1294273, 1294277, 1294301, 1294303, 1294309, 1294339, 1294351, 1294361,
    1294367, 1294369, 1294393, 1294399, 1294453, 1294459, 1294471, 1294477, 1294483,
    1294561, 1294571, 1294583, 1294597, 1294609, 1294621, 1294627, 1294633, 1294639,
    1294649, 1294651, 1294691, 1294721, 1294723, 1294729, 1294753, 1294757, 1294759,
    1294817, 1294823, 1294841, 1294849, 1294939, 1294957, 1294967, 1294973, 1294987,
    1294999, 1295003, 1295027, 1295033, 1295051, 1295057, 1295069, 1295071, 1295081,
    1295089, 1295113, 1295131, 1295137, 1295159, 1295183, 1295191, 1295201, 1295207,
    1295219, 1295221, 1295243, 1295263, 1295279, 1295293, 1295297, 1295299, 1295309,
    1295317, 1295321, 1295323, 1295339, 1295347, 1295369, 1295377, 1295387, 1295389,
    1295447, 1295473, 1295491, 1295501, 1295513, 1295533, 1295543, 1295549, 1295551,
    1295561, 1295563, 1295603, 1295611, 1295617, 1295639, 1295647, 1295653, 1295681,
    1295711, 1295717, 1295737, 1295741, 1295747, 1295761, 1295783, 1295803, 1295809,
    1295813, 1295839, 1295849, 1295867, 1295869, 1295873, 1295881, 1295947, 1295953 };

  public RandomGenerator() { this.aril = 0; }
  
  public RandomGenerator(final long seed) {
    super(seed);
    this.aril = 0;
  }

  @Override
  public synchronized void setSeed(final long seed) {
    super.setSeed(seed);
    this.aril = 0;
  }

  @Override
  protected synchronized int next(final int bits) {
    // this.aril++;
    return super.next(bits);    
  }

  @Override
  public void nextBytes(final byte[] bytes) {
    // this.aril++;
    super.nextBytes(bytes);
  }

  @Override
  public int nextInt() {
    // this.aril++;
    return super.nextInt();

  }

  @Override
  public long nextLong() {
    // this.aril++;
    return super.nextLong();
  }

  @Override
  public float nextFloat() {
    // this.aril++;
    return super.nextFloat();
  }

  @Override
  public double nextDouble() {
    this.aril++;
    return super.nextDouble();
  }

  @Override
  public synchronized double nextGaussian() {
    // this.aril++;
    return super.nextGaussian();
  }

  public long getAril() { return this.aril; }

  public void setSeed(final long seed, long cnt) {
    this.setSeed(seed);
    while (cnt-- > 0) {
      this.nextDouble();
    }
  }

  public int nextPrime() {
    int index = primes.length;
    while (index == primes.length) {
      index = (int)Math.floor(this.nextDouble() * index);
    }
    return primes[index];
  }

	public static int nextPrime(final Random r) {
		int index = primes.length;
		while (index == primes.length) {
			index = (int) Math.floor(r.nextDouble() * index);
		}
		return primes[index];
	}
}
