let
  sources = import ./nix/sources.nix;
  unstable = import sources.unstable {};
  pkgs = import sources.nixpkgs { overlays = [ (self: super: {
    jdk = super.adoptopenjdk-jre-openj9-bin-11; 
    jre = super.adoptopenjdk-jre-openj9-bin-11;
  }) ]; };

  self = {
    inherit pkgs;
    inherit unstable;
  };
in
  self
