SET INKSCAPE="C:\Program Files\Inkscape\inkscape.com"
md mipmap-xhdpi
%INKSCAPE% -z -f kkmush_icon.svg -e mipmap-xhdpi\ic_launcher_round.png -w 96 -h 96
md mipmap-xxhdpi
%INKSCAPE% -z -f kkmush_icon.svg -e mipmap-xxhdpi\ic_launcher_round.png -w 144 -h 144
md mipmap-xxxhdpi
%INKSCAPE% -z -f kkmush_icon.svg -e mipmap-xxxhdpi\ic_launcher_round.png -w 192 -h 192

%INKSCAPE% -z -f kkmush_icon.svg -e ic_launcher_round_512.png -w 512 -h 512
