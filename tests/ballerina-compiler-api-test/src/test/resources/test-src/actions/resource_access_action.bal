// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type Person record {|
   int val1;
   int val2;
|};

client class Foo {
   resource function get foo/bar/[string id](int q1, int q2) returns Person|error {
      if q1 > q2 {
         return error("Q1 > Q2");
      }
      return {val1: q1, val2: q2};
   }

   resource function get foo/[string id](int q1, int q2) returns int {
     return q1 + q2;
   }

   resource function post bar/[string... ids](string fullname) returns string {
     return ids[0] + fullname;
   }
}

public function main() {
   Foo fooClient = new();
   Person|error wVal = fooClient->/foo/bar/["3"](x, 2);
   int xVal = fooClient->/foo/["3"](1, 2);
   string yVal = fooClient->/bar/["3"].post("myname");
}

client class Bar {
    resource function get [string s1]/[string s2]() {}
}

client class Baz {
    resource function get [string... ss]() {}
}

client class Qux {
    resource function get path1/path2() {}
}

public function mmmm() {
    Bar barCl = new;

    barCl->/seg1/seg2(); // Identifier path segments
    barCl->/[...["seg1", "seg2"]](); // List constructor spread
    barCl->/["seg1"]/["seg2"]();    // Path params

    //===================================================
    Baz bazCl = new;

    bazCl ->/seg1/seg2();   // Identifier path segments
    bazCl->/[...["seg1", "seg2"]]();    // List constructor spread
    bazCl->/["seg1"]/["seg2"]();    // Path params

    //===================================================
    Qux quxCl = new;

    quxCl ->/path1/path2();   // [1] Identifier path segments
    quxCl->/[...["path1", "path2"]]();    // [2] List constructor spread
    quxCl->/["path1"]/["path2"]();    // [3] Path params

    //===================================================
    Quux quuxCl = new;

    quuxCl->/();
}

client class Quux {
    resource function get . () {}
}