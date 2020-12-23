{ src ? import ./nixpkgs.nix }:

let
  pkgs = src.pkgs;
  unstable = src.unstable;
  inherit (pkgs) stdenv;
in
  with pkgs;
  stdenv.mkDerivation rec {
    name = "Scala";

    buildInputs = [
      jdk
      sbt
      bloop
      nodejs
      yarn
    ];

    shellHook = ''
      #figlet - w160 "${name}}"
    '';
  }
