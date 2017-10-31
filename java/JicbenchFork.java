/**
 * Copyright (C) powturbo 2013-2017 GPL v2 License
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * - homepage : https://sites.google.com/site/powturbo/ - github :
 * https://github.com/powturbo - twitter : https://twitter.com/powturbo - email
 * : powturbo [_AT_] gmail [_DOT_] com
 **/
// icbench - "Integer Compression" Java Critical Native Interface

/*
 * Usage: (actually no makefile available) 1 - generate header jic.h $ cd
 * ~/TurboPFor/java $ javah -jni jic $ cp jic.h ..
 *
 * 2 - Compile jic and jicbench $ javac jic.java $ javac jicbench.java
 *
 * 3 - compile & link a shared library $ cd ~/TurboPFor $ gcc -O3 -w
 * -march=native -fstrict-aliasing -m64 -shared -fPIC
 * -I/usr/lib/jvm/default-java/include -I/usr/lib/jvm/default-java/include/linux
 * bitpack.c bitunpack.c vp4c.c vp4d.c vsimple.c vint.c bitutil.c jic.c -o
 * libic.so $ Search "/usr/lib/" for the file "jni.h" and replace the JDK name
 * "default-java" if necessary (example by "java-8-openjdk-amd64").
 *
 * 4 - copy "libic.so" to java library directory
 *
 * 5 - start icbench $java icbench
 */

class JicbenchFork {
	// Note: this is a simple interface test not a real benchmark
	private static int DEF_BYTE = 128;

	private final jic ic;
	private int[] in;
	private byte[] out;
	private int[] cpy;
	private final int testByte;

	public JicbenchFork() {
		this(DEF_BYTE);
	}

	public JicbenchFork(final int testByte) {
		this.ic = new jic();
		this.testByte = testByte;
		init();

	}

	private void init() {
		this.in = new int[256];
		this.out = new byte[256 * 5];
		this.cpy = new int[256];

		for (int i = 0; i < this.testByte; ++i) {
			this.in[i] = i;
			this.cpy[i] = 0;
		}
	}

	void bitpack32() {
		System.out.println("//bitpack32のベンチマーク--------");

		long t0 = System.currentTimeMillis();
		int b = 0, bnum = 125000000; // 16 billions integers. 64 GB
		for (int i = 0; i < bnum; ++i) {
			b = this.ic.bit32(this.in, 128);
			this.ic.bitpack32(this.in, 128, this.out, b);
		}

		long t = System.currentTimeMillis() - t0;
		System.out.printf("encode time'" + t + "'    ");
		t0 = System.currentTimeMillis();
		for (int i = 0; i < bnum; ++i) {
			this.ic.bitunpack32(this.out, 128, this.cpy, b);
		}

		for (int i = 0; i < 128; ++i) {
			if (this.in[i] != this.cpy[i]) {
				System.err.println("Error at'" + i + "'");
				System.exit(1);
			}
		}
		t = System.currentTimeMillis() - t0;
		System.out.printf("decode time'" + t + "'\n");

		init();
	}

	void p4enc32() {
		System.out.println("//p4enc32のベンチマーク--------");

		long t0 = System.currentTimeMillis();
		int b = 0, bnum = 125000000; // 16 billions integers. 64 GB
		for (int i = 0; i < bnum; ++i) {
			this.ic.p4enc32(this.in, 128, this.out);
		}

		long t = System.currentTimeMillis() - t0;
		System.out.printf("encode time'" + t + "'    ");
		t0 = System.currentTimeMillis();
		for (int i = 0; i < bnum; ++i) {
			this.ic.p4dec32(this.out, 128, this.cpy);
		}

		for (int i = 0; i < 128; ++i) {
			if (this.in[i] != this.cpy[i]) {
				System.err.println("Error at'" + i + "'");
				System.exit(1);
			}
		}
		t = System.currentTimeMillis() - t0;
		System.out.printf("decode time'" + t + "'\n");

		init();
	}

	public static void main(final String args[]) {
		int byteNum = 128;
		if (args.length > 1) {
			byteNum = Integer.parseInt(args[1]);
		}

		JicbenchFork bench = new JicbenchFork(byteNum);
		bench.bitpack32();
		bench.p4enc32();

	}

}
