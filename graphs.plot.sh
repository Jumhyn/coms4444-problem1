set terminal postscript enhanced color font 'Helvetica,18' linewidth 2
set encoding default

set mxtics
set mytics
set xrange [1:15]
set yrange [1:1000000000]
set logscale y

NAME = "./total-states-vs-winning-states"
set title "Total states vs Winning states"
set output "| ps2pdf - ".NAME.".pdf"
set key right bottom

f(x)=x**x
g(x)=gamma(x+1)

plot f(x) \
    title "# total states (n^n)" lt 1 lc rgb "#DD0000" lw 2 with lines
replot g(x) \
    title "# winning states (n!)" lt 1 lc rgb "#0000DD" lw 2 with lines

###########################################################################
###########################################################################

set terminal postscript enhanced color font 'Helvetica,18' linewidth 2
set encoding default

set mxtics
set mytics
set ylabel "avg. number of empty handles"
set xlabel "turn"
set xrange [1:900]
set yrange [1:200]

NAME = "./empty-handles-by-turn"
FILENAME = "./avg-empty-handles.txt"
set title "Averaged number of empty handles by turn (500 players, 10 iterations)"
set output "| ps2pdf - ".NAME.".pdf"

plot FILENAME \
    title "Avg. number of empty handles" lt 1 lc rgb "#DD0000" lw 2 with lines

