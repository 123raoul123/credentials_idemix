/**
 * IRMACryptoTest.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) Wouter Lueks, Radboud University Nijmegen, November 2014.
 */


package org.irmacard.credentials.idemix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.irmacard.credentials.idemix.CLSignature;
import org.irmacard.credentials.idemix.CredentialBuilder;
import org.irmacard.credentials.idemix.IdemixPublicKey;
import org.irmacard.credentials.idemix.IdemixSecretKey;
import org.irmacard.credentials.idemix.IdemixSystemParameters;
import org.irmacard.credentials.idemix.messages.IssueCommitmentMessage;
import org.irmacard.credentials.idemix.proofs.ProofU;
import org.irmacard.credentials.idemix.util.Crypto;
import org.junit.Test;

public class IRMACryptoTest {
	static BigInteger p = new BigInteger("10436034022637868273483137633548989700482895839559909621411910579140541345632481969613724849214412062500244238926015929148144084368427474551770487566048119");
	static BigInteger q = new BigInteger("9204968012315139729618449685392284928468933831570080795536662422367142181432679739143882888540883909887054345986640656981843559062844656131133512640733759");

	static BigInteger n = new BigInteger("96063359353814070257464989369098573470645843347358957127875426328487326540633303185702306359400766259130239226832166456957259123554826741975265634464478609571816663003684533868318795865194004795637221226902067194633407757767792795252414073029114153019362701793292862118990912516058858923030408920700061749321");
	static BigInteger S = new BigInteger("68460510129747727135744503403370273952956360997532594630007762045745171031173231339034881007977792852962667675924510408558639859602742661846943843432940752427075903037429735029814040501385798095836297700111333573975220392538916785564158079116348699773855815825029476864341585033111676283214405517983188761136");
	static BigInteger Z = new BigInteger("44579327840225837958738167571392618381868336415293109834301264408385784355849790902532728798897199236650711385876328647206143271336410651651791998475869027595051047904885044274040212624547595999947339956165755500019260290516022753290814461070607850420459840370288988976468437318992206695361417725670417150636");

	static List<BigInteger> R = Arrays.asList(
			new BigInteger("75350858539899247205099195870657569095662997908054835686827949842616918065279527697469302927032348256512990413925385972530386004430200361722733856287145745926519366823425418198189091190950415327471076288381822950611094023093577973125683837586451857056904547886289627214081538422503416179373023552964235386251"),
			new BigInteger("16493273636283143082718769278943934592373185321248797185217530224336539646051357956879850630049668377952487166494198481474513387080523771033539152347804895674103957881435528189990601782516572803731501616717599698546778915053348741763191226960285553875185038507959763576845070849066881303186850782357485430766"),
			new BigInteger("13291821743359694134120958420057403279203178581231329375341327975072292378295782785938004910295078955941500173834360776477803543971319031484244018438746973179992753654070994560440903251579649890648424366061116003693414594252721504213975050604848134539324290387019471337306533127861703270017452296444985692840"),
			new BigInteger("86332479314886130384736453625287798589955409703988059270766965934046079318379171635950761546707334446554224830120982622431968575935564538920183267389540869023066259053290969633312602549379541830869908306681500988364676409365226731817777230916908909465129739617379202974851959354453994729819170838277127986187"),
			new BigInteger("68324072803453545276056785581824677993048307928855083683600441649711633245772441948750253858697288489650767258385115035336890900077233825843691912005645623751469455288422721175655533702255940160761555155932357171848703103682096382578327888079229101354304202688749783292577993444026613580092677609916964914513"),
			new BigInteger("65082646756773276491139955747051924146096222587013375084161255582716233287172212541454173762000144048198663356249316446342046266181487801411025319914616581971563024493732489885161913779988624732795125008562587549337253757085766106881836850538709151996387829026336509064994632876911986826959512297657067426387"));

	static IdemixSecretKey sk = new IdemixSecretKey(p, q);
	static IdemixPublicKey pk = new IdemixPublicKey(n, Z, S, R);

	@Test
	public void testPublicKey() {
		assertEquals(p.multiply(q), n);
	}

	@Test
	public void testCLSignature() {
		List<BigInteger> ms = new Vector<BigInteger>();
		ms.add(new BigInteger("1"));
		ms.add(new BigInteger("2"));
		ms.add(new BigInteger("3"));

		CLSignature sig = CLSignature.signMessageBlock(sk, pk, ms);
		assertTrue("Signature is not valid", sig.verify(pk, ms));

		ms.set(0, new BigInteger("1337"));
		assertFalse("Signature should not verify", sig.verify(pk, ms));
	}

	@Test
	public void testASN1Encoding1() {
		byte[] enc = Crypto.asn1Encode(new BigInteger("1"),
				new BigInteger("65"), new BigInteger("1025"));

		byte[] expected = { 0x30, 0x0D,
				0x02, 0x01, 0x03, // The number of elements is additionally encoded
				0x02, 0x01, 0x01,
				0x02, 0x01, 0x41,
				0x02, 0x02, 0x04, 0x01 };

		assertTrue(Arrays.equals(enc, expected));
	}

	@Test
	public void testProofU() {
		Random rnd = new Random();
		IdemixSystemParameters params = pk.getSystemParameters();

		BigInteger context = new BigInteger(params.l_h, rnd);
		BigInteger n_1 = new BigInteger(params.l_statzk, rnd);
		BigInteger secret = new BigInteger(params.l_m, rnd);

		CredentialBuilder cb = new CredentialBuilder(pk, null, context);
		cb.setSecret(secret);

		BigInteger U = cb.commitmentToSecret();
		ProofU proofU = cb.createProofU(U, n_1);

		assertTrue(proofU.verify(pk, U, context, n_1));
	}

	@Test
	public void testProofULogged() {
		BigInteger context = new BigInteger("34911926065354700717429826907189165808787187263593066036316982805908526740809");
		BigInteger n_1 = new BigInteger("724811585564063105609243");
		BigInteger c = new BigInteger("4184045431748299802782143929438273256345760339041229271411466459902660986200");
		BigInteger U = new BigInteger("53941714038323323772993715692602421894514053229231925255570480167011458936488064431963770862062871590815370913733046166911453850329862473697478794938988248741580237664467927006089054091941563143176094050444799012171081539721321786755307076274602717003792794453593019124224828904640592766190733869209960398955");
		BigInteger v_prime_response = new BigInteger("930401833442556048954810956066821001094106683380918922610147216724718347679854246682690061274042716015957693675615113399347898060611144526167949042936228868420203309360695585386210327439216083389841383395698722832808268885873389302262079691644125050748391319832394519920382663304621540520277648619992590872190274152359156399474623649137315708728792245711389032617438368799004840694779408839779419604877135070624376537994035936");
		BigInteger s_response = new BigInteger("59776396667523329313292302350278517468587673934875085337674938789292900859071752886820910103285722288747559744087880906618151651690169988337871960870439882357345503256963847251");

		ProofU proofU = new ProofU(c, v_prime_response, s_response);

		assertTrue(proofU.verify(pk, U, context, n_1));
	}

	@Test
	public void testCommitmentMessage() {
		Random rnd = new Random();
		IdemixSystemParameters params = pk.getSystemParameters();

		BigInteger context = new BigInteger(params.l_h, rnd);
		BigInteger n_1 = new BigInteger(params.l_statzk, rnd);
		BigInteger secret = new BigInteger(params.l_m, rnd);

		CredentialBuilder cb = new CredentialBuilder(pk, null, context);
		IssueCommitmentMessage msg = cb.commitToSecretAndProve(secret, n_1);
		assertTrue(msg.getCommitmentProof().verify(pk, msg.getCommitment(), context, n_1));
	}
}