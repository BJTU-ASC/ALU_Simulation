/**
 * Copyright (c) BJTU Academic Support Centre. All Rights Reserved
 *
 * @author (Hanqi Jiang)
 * @version (20220404)
 */
class ALU {
  public int c;  
  public int sf, zf, of, cf; 

  public void compute(int a, int b, int op) {
    a = a & 0xFF; 
    b = b & 0xFF; 
    op = op & 3; 
    switch (op) {
      case (0): add(a, b); break; 
      case (1): sub(a, b); break; 
      case (2): mul(a, b); break; 
      case (3): div(a, b); break; 
    }
    this.c &= 0xFF; 
  }

  private void add(int a, int b) {
    int cin = (a & 0x7F) + (b & 0x7F) >> 7 & 1; 
    int cout = a + b >> 8 & 1; 
    this.c = a + b; 
    this.of = cin != cout ? 1 : 0; 
    this.cf = cout;
    this.sf = (a >> 7 ^ b >> 7) == 1 ? cin ^ 1 : a >> 7; 
    this.zf = (this.of ^ 1) & ((this.c & 0xFF) == 0 ? 1 : 0); 
  }

  private void sub(int a, int b) {
    int neg_b = -b & 0xFF; 
    add(a, neg_b); 
    int b_is_min = neg_b == b ? 1 : 0; 
    this.of |= b_is_min; 
    this.cf = this.sf & (b_is_min ^ 1); 
    this.sf &= b_is_min ^ 1; 
  }

  private void mul(int a, int b) {
    int acc = 0; 
    a = a << 24 >> 24; 
    for (int i = 0; i < 7; i++, b >>= 1, a <<= 1) 
      if ((b & 1) == 1) acc += a; 
    if ((b & 1) == 1) acc += -a;
    this.c = acc;
    int pad = acc & 0xFF80; 
    this.of = pad == 0xFF80 || pad == 0 ? 0 : 1; 
    this.sf = acc >> 15 & 1; 
    this.zf = acc == 0 ? 1 : 0; 
  }

  private void div(int a, int b) {
    int abs_a = a >> 7 == 0 ? a : -a & 0xFF; 
    int abs_b = b >> 7 == 0 ? b : -b & 0xFF; 
    int r1 = 0; 
    int r0 = abs_a; 
    for (int i = 0; i < 8; i++) {
      int temp = (r1 << 8 | r0) << 1 & 0xFFFF; 
      r1 = temp >> 8; 
      r0 = temp & 0xFF; 
      r1 = r1 - abs_b & 0xFF; 
      if (r1 >> 7 == 0)
        r0 |= 1; 
      else {
        r0 &= 0xFE;
        r1 = r1 + abs_b & 0xFF; 
      }
    }
    this.zf = abs_a < abs_b ? 1 : 0; 
    this.sf = (a ^ b) >> 7 & (this.zf ^ 1);
    this.c = this.sf == 0 ? r0 : -r0; 
    this.of = a == 0x80 && b == 1 ? 1 : 0; 
  }
}

public class Test_ALU {
  public static void main(String[] args) {
    ALU alu = new ALU(); 

    alu.compute(50, -7, 3); 
    System.out.println("of=" + alu.of + " cf=" + alu.cf + " sf=" + alu.sf + " zf=" + alu.zf); 

    int temp = alu.c, val = 0; 
    String bin = ""; 
    for (int i = 0, w = 1; i < 8; i++, temp >>= 1, w *= 2) 
      if ((temp & 1) == 1) {
        bin = "1" + bin; 
        val += i == 7 ? -w : w; 
      } else {
        bin = "0" + bin; 
      }
    System.out.println(val); 
    System.out.println(bin); 
  }
}