# Integral Problem Set

## 1 $\int\frac{1}{5x+3}dx$

$$
=\frac{1}{5}\int\frac{5}{5x+3}dx\\
=\frac{1}{5}\ln{|5x+3|}+C
$$

## 2 $\int e^{2x+3}dx$

$$
=\frac{1}{2}\int e^{2x+3}d(2x+3)\\
=\frac{1}{2}e^{2x+3}+C
$$

## 3 $\int xe^{x^2}dx$

$$
=\frac{1}{2}\int e^{x^2}dx^2\\
=\frac{1}{2}e^{x^2} + C
$$

## 4 $\int x\sqrt{1-x^2}dx$

$$
=-\frac{1}{2}\int\sqrt{1-x^2}d(1-x^2)\\
=-\frac{1}{2}\cdot\frac{2}{3}(1-x^2)^{\frac{3}{2}}+C\\
=-\frac{1}{3}(1-x^2)^{\frac{3}{2}}+C
$$

## 5 $\int \frac{1}{x^2}\sin \frac{1}{x}dx$

$$
= \int -\sin \frac{1}{x}d\frac{1}{x} \\
=\cos \frac{1}{x} +  C
$$

## 6 $\int \frac{e^{ 3\sqrt{x} } }{\sqrt{x} }dx$

$$
=2\int \frac{1}{2\sqrt{x}} e^{3\sqrt{x}}dx\\
=\frac{2}{3}\int e^{3\sqrt{x}}d(3\sqrt{x})\\
=\frac{2}{3}e^{3\sqrt{x}}+C
$$

## 7 $\int \frac{1}{x(1+x^6)}dx$

$$
=\frac{1}{6}\int\frac{6x^5}{x^6(1+x^6)}dx\\
=\frac{1}{6}\int\frac{1}{x^6(1+x^6)}dx^6\\
=\frac{1}{6}\int(\frac{1}{x^6}-\frac{1}{x^6+1})dx^6\\
=\frac{1}{6}\ln{x^6}-\frac{1}{6}\ln{(x^6+1)}+C\\
=\frac{1}{6}\ln\frac{x^6}{x^6+1}+C
$$

## 8 $\int\cos{2x}dx$

$$
=\frac{1}{2}\int\cos {2x}d2x\\
=\frac{1}{2}\sin{2x}+C
$$

## 9 $\int\frac{\sin{x} }{\sqrt{5+\cos{x} } }$

$$
=-\int\frac{1}{\sqrt{5+\cos{x}}}d\cos{x}\\
=-2\sqrt{5+\cos{x}}+C
$$

## 10 $\int\tan^4{x}dx$

$$
=\int\tan^2x(\sec^2x-1)dx\\
=\int\tan^2x\sec^2xdx-\int\tan^2xdx\\
=\int\tan^2xd\tan{x}-\int(\sec^2x-1)dx\\
=\frac{1}{3}\tan^3x-\int\sec^2xdx+\int dx\\
=\frac{1}{3}\tan^3x-\tan{x}+x+C
$$

## 11 $\int\frac{e^{2x} }{e^x+1}dx$

$$
=\int\frac{e^x}{e^x+1}de^x\\
=\int(1-\frac{1}{e^x+1})de^x\\
=\int de^x-\int\frac{1}{e^x+1}d(e^x+1)\\
=e^x-\ln{(e^x+1)}+C
$$

## 12 $\int\frac{1}{1+e^x}dx$

$$
=\int(1-\frac{e^x}{1+e^x}dx)\\
=\int dx-\int\frac{1}{1+e^x}d(e^x+1)\\
=x-\ln{(e^x+1)}+C
$$

## 13 $\int \frac{1}{x\ln^2x}dx$

$$
=\int\frac{\frac{1}{x}}{ln^2x}dx\\
=\int\frac{1}{\ln^2x}d\ln x\\
=-\frac{1}{lnx}+C
$$

## 14 $\int\frac{1}{x(1+2\ln x)}dx$

$$
=\int\frac{\frac{1}{x}}{1+2\ln x}dx\\
=\int\frac{1}{1+2\ln x}d\ln x\\
=\frac{1}{2}\int\frac{1}{1+2\ln x}d(2\ln x+1)\\
=\frac{1}{2}\ln{|2\ln x+1|} + C
$$

## 15 $\int\frac{1}{a^2\cos^2x+b^2\sin^2x}dx$

$$
=\frac{1}{b^2}\int\frac{1}{\frac{a^2}{b^2}\cos^2x+\sin^2x}dx\\
=\frac{1}{b^2}\int\frac{\sec^2x}{\frac{a^2}{b^2}+\tan^2x}dx \\
\text{let }t=\tan x,x=\tan^{-1}t,-\frac{\pi}{2}\le x\le\frac{\pi}{2}\\
=\frac{1}{b^2}\int\frac{t^2+1}{\frac{a^2}{b^2}+t^2}\cdot\frac{1}{1+t^2}dt\\
=\frac{1}{b^2}\int\frac{1}{\frac{a^2}{b^2}+t^2}dt\\
\text{let }t=\frac{a}{b}s,s=\frac{b}{a}t\\
=\frac{1}{a^2}\cdot\frac{a}{b}\int\frac{1}{1+s^2}ds\\
=\frac{1}{ab}\tan^{-1}\frac{b}{a}t+C\\
=\frac{1}{ab}\tan^{-1}(\frac{b}{a}\tan x) + C
$$

## 16 $\int\frac{1}{a^2+x^2}dx$

$$
=a\int\frac{1}{a^2(1+(\frac{x}{a})^2)}d\frac{x}{a}\\
=\frac{1}{a}\int\frac{1}{1+(\frac{x}{a})^2}d\frac{x}{a}\\
=\frac{1}{a}\tan^{-1}\frac{x}{a}+C
$$

## 17 $\int\frac{1}{a^2-x^2}dx$

$$
=\frac{1}{2a}\int\frac{2a}{(a+x)(a-x)}dx\\
=\frac{1}{2a}\int(\frac{1}{a+x}+\frac{1}{a-x})dx\\
=\frac{1}{2a}\ln|a+x|-\frac{1}{2a}\ln|a-x|+C\\
=\frac{1}{2a}\ln|\frac{a+x}{a-x}|+C
$$

## 18 $\int\frac{1}{\sqrt{a^2-x^2} }dx$

$$
=\int\frac{1}{\sqrt{1-(\frac{x}{a})^2}}d\frac{x}{a}\\
=\sin^{-1}\frac{x}{a}+C
$$

## 19 $\int\sin^3xdx$

$$
=-\int(1-\cos^2x)d\cos x\\
=\int\cos^2xd\cos x-\int d\cos x\\
=\frac{1}{3}cos^3x-\cos x+C
$$

## 20 $\int\sin^5xdx$

$$
=\int\sin^3x(1-\cos^2x)dx\\
=\int\sin^3dx+\int\cos^2x(1-\cos^2)d\cos x\\
=\frac{1}{3}\cos^3x-\cos x+\frac{1}{3}\cos^3x-\frac{1}{5}\cos^5x+C\\
=-\frac{1}{5}\cos^5x+\frac{2}{3}\cos^3x+\cos x+C
$$

## 21 $\int\cos^3xdx$

$$
=\int(1-\sin^2x)dx\sin x\\
=\int d\sin x-\int\sin^2xd\sin x\\
=\sin x-\frac{1}{3}\sin^3x +C
$$

## 22 $\int\sin^4xdx$

$$
=\int(sin^2x)^2dx\\
=\int(\frac{1}{2}(1-\cos2x))^2dx\\
=\frac{1}{4}\int(\cos^22x-2\cos 2x+1)dx\\
=\frac{1}{4}\int\cos^22xdx-\frac{1}{4}\int\cos 2xd(2x)+\frac{1}{4}\int dx\\
=\frac{1}{32}\int(1+\cos 4x)d(4x)-\frac{1}{4}\sin 2x+\frac{1}{4}x\\
=\frac{1}{8}x+\frac{1}{32}\sin 4x-\frac{1}{4}\sin 2x+\frac{1}{4}x+C\\
=\frac{1}{32}\sin 4x-\frac{1}{4}\sin 2x+\frac{3}{8}x+C
$$

## 23 $\int\sin^2x\cos^5xdx$

$$
=\int\sin^2x(1-\sin^2x)^2d\sin x\\
=\int(sin^6x-2\sin^4x+\sin^2x)d\sin x\\
=\frac{1}{7}\sin^7x-\frac{2}{5}\sin^5x+\frac{1}{3}\sin^3x+C
$$

## 24 $ \int\sec xdx$

$$
=\int\sec x\cdot\frac{\sec x+ \tan x}{\sec x+ \tan x}dx\\
=\int\frac{\sec^2x+\sec x\tan x}{\sec x+ \tan x}dx\\
=\ln|\sec x+\tan x|+C\\
$$

## 25 $\int\sec^3x\tan^5xdx$

$$
=\int\sec^2x(\sec^2x-1)^2d\sec x\\
=\int(\sec^6x-2\sec^4x+\sec^2x)d\sec x\\
=\frac{1}{7}\sec^7x-\frac{2}{5}\sec^5x+\frac{1}{3}\sec^3x+C
$$

## 26 $\int\tan^5x\sec^4xdx$

$$
=\int(\sec^2x-1)^2\sec^3xd\sec x\\
=\int(\sec^7x-2\sec^5x+\sec^3x)d\sec x\\
=\frac{1}{8}\sec^8x -\frac{1}{3}\sec^6+\frac{1}{4}\sec^4x+C
$$

## 27 $\int\frac{\ln(\tan x)}{\sin x\cos x}dx$

$$
\text{note that }\frac{d}{dx}\ln(\tan x)=\sec^2x\cdot\frac{1}{\tan x}=\frac{1}{\sin x\cos x}\\
integral=\int\ln(\tan x)d(\ln(\tan x))\\
=\frac{1}{2}\ln^2(\tan x)+C
$$

## 28 $\int\cos 2x\cos 3xdx$

$$
=\int(1-2\sin^2x)(\cos 2x\cos x-\sin 2x\sin x)dx\\
=\int(1-2\sin^2x)(1-2\sin^2x-2\sin^2x)d\sin x\\
=\int(8\sin^4-6\sin^2x+1)d\sin x\\
=\frac{8}{5}\sin^5x-2\sin^3x+\sin x+C
$$

## 29 $\int\frac{\sin x}{1+\sin x}dx$

$$
=\int\frac{\sin x-\sin^2x}{1-\sin^2x}dx\\
=\int(\frac{\sin x}{\cos^2x}-\tan^2x)dx\\
=\int(\sec x\tan x-\sec^2x+1)dx\\
=\sec x-\tan x+x+C
$$

## 30 $\int\frac{1}{\sin 2x\cos x}dx$

$$
=\int\frac{1}{2\sin x\cos^2x}dx\\
=\int\frac{1}{2\sin x}d\tan x\\
=\frac{\tan x}{2\sin x}-\frac{1}{2}\int\tan xd\csc x\\
=\frac{\tan x}{2\sin x}+\frac{1}{2}\int\tan x\csc x\cot xdx\\
=\frac{\tan x}{2\sin x}+\frac{1}{2}\ln|\csc x-\cot x|+C
$$

## 31 $\int\frac{\tan^{-1}\sqrt{x} }{\sqrt{x} (1+x)}dx$

$$
\text{let }t=\sqrt{x}\\
integral=\int\frac{\tan^{-1}t}{t(1+t^2)}dt^2\\
=2\int\frac{\tan^{-1}t}{1+t^2}dt\\
=2\int\tan^{-1}td\tan^{-1}t\\
=(\tan^{-1}t)^2+C
$$

## 32 $\int\frac{1+\ln x}{2+(x\ln x)^2}dx$

$$
\text{note that }\frac{d}{dx}x\ln x=\ln x+1\\
integral=\int\frac{1}{2+(x\ln x)^2}d(x\ln x)\\
=\frac{1}{\sqrt{2}}\int\frac{1}{1+(\frac{x\ln x}{\sqrt{2}})^2}d\frac{x\ln x}{\sqrt{2}}\\
=\frac{1}{\sqrt{2}}\tan^{-1}\frac{x\ln x}{\sqrt{2}}+C
$$

## 33 $\int\frac{2x-3}{x^2-3x+1}dx$

$$
\text{note that }\frac{d}{dx}x^2-3x+1=2x-3\\
integral=\int\frac{1}{x^2-3x+1}d(x^3-3x+1)\\
=ln|x^2-3x+1|+C
$$

## 34 $\int\frac{x+1}{x^2-3x+1}dx$

$$
=\frac{1}{2}\int\frac{2x+2}{x^2-3x+1}dx\\
=\frac{1}{2}\int\frac{2x-3+5}{x^2-3x+1}dx\\
=\frac{1}{2}\int\frac{2x-3}{x^2-3x+1}dx+\frac{5}{2}\int\frac{1}{(x-\frac{3}{2})^2-(\frac{\sqrt{5}}{2})^2}dx\\
=\frac{1}{2}\ln|x^2-3x+1|+\frac{5}{2}\cdot\frac{1}{\sqrt{5}}\ln|\frac{\frac{\sqrt{5}}{2}+x-\frac{3}{2}}{\frac{\sqrt{5}}{2}-x+\frac{3}{2}}|+C\\
=\frac{1}{2}\ln|x^2-3x+1|+\frac{\sqrt{5}}{2}\ln|\frac{\sqrt{5}-3+2x}{\sqrt{5}+3-2x}|+C
$$

## 35 $\int\frac{1-\ln x}{(x-\ln x)^2}dx$

$$
=\int\frac{\frac{1-\ln x}{x^2}}{(1-\frac{\ln x}{x})^2}dx\\
=\int\frac{1}{(1-\frac{\ln x}{x})^2}d\frac{\ln x}{x}\\
=\int-\frac{1}{(1-\frac{\ln x}{x})^2}d(1-\frac{\ln x}{x})\\
=\frac{1}{1-\frac{\ln x}{x}}+C\\
=\frac{x}{x-\ln x} + C
$$

## 36 $\int\sqrt{a^2-x^2}dx$

$$
\text{let }x=a\sin t,t=\sin^{-1}\frac{x}{a},-\frac{\pi}{2}\le t\le\frac{\pi}{2}\\
integral=\int a\cos tda\sin t\\
=a^2\int\cos^2tdt\\
=\frac{a^2}{4}\int(\cos 2t+1)d2t\\
=\frac{a^2}{4}\sin 2t+\frac{a^2}{2}t+C\\
=\frac{x}{2}\sqrt{a^2-x^2}+\frac{a^2}{2}\sin^{-1}\frac{x}{a}+C
$$

## 37 $\int\frac{1}{\sqrt{x^2-a^2} }dx$

$$
\text{let }x=a\sec t,t=\sec^{-1}\frac{x}{a},0\le t \le\pi,t\neq\frac{\pi}{2}\\
\sec t=\frac{x}{a},\tan  t=\begin{cases}\sqrt{\frac{x^2-a^2}{a^2}},0\le t<\frac{\pi}{2}\\
-\sqrt{\frac{x^2-a^2}{a^2}},\frac{\pi}{2}<t\le\pi\end{cases}\\
integral=\frac{1}{a}\int\frac{1}{|\tan t|}d(a\sec t)\\
=\int\frac{\sec t\tan t}{|\tan t|}dt\\
=\frac{\tan t}{|\tan t|}\ln|\sec t +\tan t|+C\\
=\begin{cases}
\ln|\frac{x}{a}+\sqrt{\frac{x^2-a^2}{a^2}}|+C,x>a\\
-\ln|\frac{x}{a}-\sqrt{\frac{x^2-a^2}{a^2}}|+C,x>-a
\end{cases}\\
=\begin{cases}
\ln|x+\sqrt{x^2-a^2}|-\ln a+C,x>a\\
\ln|\frac{1}{x-\sqrt{x^2-a^2}}|-\ln a+C,x>-a
\end{cases}\\
=\begin{cases}
\ln|x+\sqrt{x^2-a^2}|+C,x>a\\
\ln|\frac{x+\sqrt{x^2-a^2}}{a^2}|+C,x<-a\\
\end{cases}\\
=\ln|x+\sqrt{x^2-a^2}|+C
$$

## 38 $\int\frac{1}{\sqrt{a^2+x^2} }dx$

$$
\text{let }x=a\tan t,t=\tan^{-1}\frac{x}{a},-\frac{\pi}{2}< t<\frac{\pi}{2}\\
\tan t=\frac{x}{a},\sec t=\sqrt{1+\tan^2t}=\sqrt{\frac{x^2+a^2}{a^2}}\\
integral=\frac{1}{a}\int\frac{1}{\sqrt{1+\tan^2t}}da\tan t\\
=\int\sec tdt\\
=\ln|\sec t+\tan t|+C\\
=\ln|\frac{\sqrt{x^2+a^2}}{a}+\frac{x}{a}|+C\\
=\ln(\sqrt{x^2+a^2}+x)-\ln a+C\\
=\ln(\sqrt{x^2+a^2}+x)+C
$$

## 39 $\int\frac{1}{x^{\frac{1}{2} }+x^{\frac{1}{3} } }dx$

$$
\text{let }x=t^6,t=x^{\frac{1}{6}}\\
integral=\int\frac{1}{t^3+t^2}dt^6\\
=6\int\frac{t^5}{t^3+t^2}dt\\
=6\int\frac{t^3}{t+1}dt\\
=6\int(t^2-t+1-\frac{1}{t+1})dt\\
=2t^3-3t^2+6t-6\ln(t+1)+C\\
=2x^{\frac{1}{2}}-3x^{\frac{1}{3}}+6x^{\frac{1}{6}}-6\ln(x^{\frac{1}{6}}+1)+C
$$

## 40 $\int\frac{1}{(1+x^2)^2}dx$

$$
\text{let }x=\tan t,t=\tan^{-1}x\\
integral=\int\frac{1}{\sec^4t}d\tan t\\
=\int\frac{1}{sec^2t}dt\\
=\int\cos^2tdt\\
=\frac{1}{4}\int(\cos2t+1)d2t\\
=\frac{1}{4}\sin2t+\frac{1}{2}t+C\\
=\frac{x}{2+2x^2}+\frac{1}{2}\tan^{-1}x+C
$$

## 41 $\int\frac{3x+1}{\sqrt{x^2+2x-5} }dx$

$$
=\int\frac{3(x+1)-2}{\sqrt{(x+1)^2-6}}d(x+1)\\
=3\int\frac{1}{2\sqrt{(x+1)^2-6}}d(x+1)^2-2\int\frac{1}{\sqrt{(x+1)^2-6}}d(x+1)\\
=3\sqrt{(x+1)^2-6}-2\ln|x+1-\sqrt{(x+1)^2-6}|+C
$$

## 42 $\int\frac{1}{x^2\sqrt{x^2-1} }dx$

$$
\text{let }x=\sec t,t=\sec^{-1}x,0<t<\pi,t\neq\frac{\pi}{2}\\
integral=\int\frac{1}{\sec^2t|\tan t|}d\sec t\\
=\int\frac{\tan t}{\sec t|\tan t|}dt\\
=\begin{cases}
\int \cos tdt,0<t<\frac{\pi}{2}\\
-\int \cos tdt,\frac{\pi}{2}<t<\pi
\end{cases}\\
=\begin{cases}
\sin t+C,0<t<\frac{\pi}{2}\\
-\sin t+C,\frac{\pi}{2}<t<\pi
\end{cases}\\
=\begin{cases}
\sqrt{\frac{x^2-1}{x^2}}+C,x>1\\
-\sqrt{\frac{x^2-1}{x^2}}+C,x<-1
\end{cases}\\
=\frac{\sqrt{x^2-1}}{x}+C
$$

## 43 $\int\frac{1}{x^6(x^2+1)}dx$

$$
\text{let }x=\tan t\\
=\int\frac{1}{\tan^6t\sec^2t}d\tan t\\
=\int\frac{1}{\tan^6t}dt\\
=\int\cot^6tdt\\
=\int\cot^4t(\csc^2t-1)dt\\
=\int\cot^4t\csc^2tdt-\int\cot^4tdt\\
=-\int\cot^4td\cot t-\int\cot^2t(\csc^2t-1)dt\\
=-\frac{1}{5}\cot^5t-\int\cot^2td\cot t+\int(\csc^2t-1)dt\\
=-\frac{1}{5}\cot^5t+\frac{1}{3}\cot^3t-\cot t-t+C\\
=-\frac{1}{5x^5}+\frac{1}{3x^3}-\frac{1}{x}-\tan^{-1}x+C
$$

## 44 $\int\sqrt{1+\sqrt{x} }dx$

$$
\text{let }t=\sqrt{1+\sqrt{x}},x=(t^2-1)^2\\
integral=\int t\cdot2t\cdot2(t^2-1)dt\\
=4\int (t^4-t^2) dt\\
=\frac{4}{5}t^5-\frac{4}{3}t^3+C\\
=\frac{4}{5}\bigg(\sqrt{1+\sqrt{x}}\bigg)^5-\frac{4}{3}\bigg(\sqrt{1+\sqrt{x}}\bigg)^3+C
$$

## 45 $\int x\cos xdx$

$$
=\int xd\sin x\\
=x\sin x-\int\sin xdx\\
=x\sin x+\cos x+C
$$

## 46 $\int x^2\cos xdx$

$$
=\int x^2d\sin x\\
=x^2\sin x-\int\sin xdx^2\\
=x^2\sin x-2\int x\sin xdx\\
=x^2\sin x+2\int xd\cos x\\
=x^2\sin x+2x\cos x-2\int\cos xdx\\
=x^2\sin x+2x\cos x-2\sin x+C
$$

## 47 $\int xe^xdx$

$$
=\int xde^x\\
=xe^x-\int e^xdx\\
=xe^x-e^x+C
$$

## 48 $\int x^2e^xdx$

$$
=\int x^2de^x\\
=x^2e^x-\int e^xdx^2\\
=x^2e^x-2\int xe^xdx\\
=x^2e^x-2\int xde^x\\
=x^2e^x-2xe^x+2\int e^xdx\\
=x^2e^x-2xe^x+2e^x+C
$$

## 49 $\int x^2\cos^2\frac{x}{2}dx$

$$
=\int x^2\frac{1}{2}(\cos x+1)dx\\
=\frac{1}{2}\int(x^2\cos x+x^2)dx\\
=\frac{1}{2}x^2\sin x+x\cos x-\sin x+\frac{1}{6}x^3+C
$$

## 50 $\int x\tan^2xdx$

$$
=\int(x\sec^2x-x)dx\\
=\int xd\tan x-\frac{1}{2}x^2\\
=x\tan x-\int\tan xdx-\frac{1}{2}x^2\\
=x\tan x+\ln|\cos x|-\frac{1}{2}x^2+C
$$

## 51 $\int x\ln xdx$

$$
=\frac{1}{2}\int \ln x dx^2\\
=\frac{1}{2}x^2\ln x-\frac{1}{2}\int x^2d\ln x\\
=\frac{1}{2}x^2\ln x-\frac{1}{2}\int xdx\\
=\frac{1}{2}x^2\ln x-\frac{1}{4} x^2 +C
$$

## 52 $\int \ln xdx$

$$
=x\ln x-\int x\ln x\\
=x\ln x-x+C
$$

## 53 $\int\frac{1}{1+\cos x}dx$

$$
=2\int \frac{1}{2\cos^2\frac{x}{2}}dx\\
=2\int\sec^2\frac{x}{2}d\frac{x}{2}\\
=2\tan\frac{x}{2}+C
$$

## 54 $\int\frac{1}{1+\sin x}dx$

$$
=\int\frac{1-\sin x}{1-\sin^2x}dx\\
=\int\sec^2xdx+\int\frac{1}{\cos^2x}d\cos x\\
=\tan x-\frac{1}{\cos x}+C
$$

## 55 $\int\frac{x}{x^4+4}dx$

$$
=\frac{1}{4}\int\frac{1}{(\frac{x^2}{2})^2+1}d\frac{x^2}{2}\\
=\frac{1}{4}\tan^{-1}\frac{x^2}{2}+C
$$

## 56 $\int\frac{x^2+2}{(x+1)^3}dx$

$$
=\int\frac{x^2+2x+1-2x-2+3}{(x+1)^3}dx\\
=\int\frac{1}{x+1}dx-2\int\frac{1}{(x+1)^2}dx+3\int\frac{1}{(x+1)^3}dx\\
=\ln|x+1|+2\frac{1}{x+1}-\frac{3}{2(x+1)^2}+C
$$

## 57 $\int\frac{x^5}{\sqrt{1-x^2} }dx$

$$
\text{let }x=\sin t,-\frac{\pi}{2}<t<\frac{\pi}{2}\\
integral=\int\frac{\sin^5t}{\cos t}d\sin t\\
=\int\sin^5tdt\\
=-\int(1-\cos^2t)^2d\cos t\\
=-\int(\cos^4t -2\cos^2t+1)d\cos t\\
=-\frac{1}{5}\cos^5t +\frac{2}{3}\cos^3t-\cos t+C\\
=-\frac{1}{5}(\sqrt{1-x^2})^5+\frac{2}{3}(\sqrt{1-x^2})^3-\sqrt{1-x^2}+C
$$

## 58 $\int\frac{\sqrt{x+1}-1}{\sqrt{x+1}+1}dx$

$$
\text{let }t=\sqrt{x+1}-1,t+2=\sqrt{x+1}+1,x=t(t+2)\\
integral=\int\frac{t}{t+2}(2t+2)dt\\
=2\int\frac{t^2+2t-t-2+2}{t+2}dt\\
=2\int tdt-2\int dt+4\int\frac{1}{t+2}dt\\
=t^2-2t+4\ln|t+2|+C\\
=x-2\sqrt{x+1}+2-2\sqrt{x+1}+2+4\ln|\sqrt{x+1}+1|+C\\
=x-4\sqrt{x+1}+4\ln|\sqrt{x+1}+1|+C
$$

## 59 $\int x(1-2x)^{99}dx$

$$
=-\frac{1}{2}\int x(1-2x)^{99}d(1-2x)\\
=-\frac{1}{200}\int xd(1-2x)^{100}\\
=-\frac{x(1-2x)^{100}}{200}+\frac{1}{200}\int (1-2x)^{100}dx\\
=-\frac{x(1-2x)^{100}}{200}-\frac{1}{400}\int(1-2x)^{100}d(1-2x)\\
=-\frac{x(1-2x)^{100}}{200}-\frac{(1-2x)^{101}}{40400}+C
$$

## 60 $\int\frac{1}{x\ln x\ln(ln x)}dx$

$$
\text{let }t=|\ln x|,dt=\frac{dx}{x}\\
integral=\int\frac{1}{t\ln t}dt\\
=\ln|\ln t|+C\\
=\ln|\ln |\ln x||+C
$$

## 61 $\int\frac{x^7}{x^4+2}dx$

$$
=\frac{1}{8}\int\frac{1}{x^4+2}dx^8\\
\text{let }t=x^4\\
integral=\frac{1}{8}\int\frac{1}{t+2}dt^2\\
=\frac{1}{8}\int\frac{2t+4-4}{t+2}dt\\
=\frac{1}{4}t-\frac{1}{2}\ln|t+2|+C\\
=\frac{x^4}{4}-\frac{1}{2}\ln|x^4+2|+C
$$

## 62 $\int(\sin^{-1}x)^2dx$

$$
=x(\sin^{-1}x)^2-\int xd(\sin^{-1}x)^2\\
=x(\sin^{-1}x)^2-\int x\frac{2\sin^{-1}x}{\sqrt{1-x^2}}dx\\
=x(\sin^{-1}x)^2+\int \frac{\sin^{-1}x}{\sqrt{1-x^2}}d(1-x^2)\\
=x(\sin^{-1}x)^2+2\int \sin^{-1}xd\sqrt{1-x^2}\\
=x(\sin^{-1}x)^2+2\sin^{-1}x\sqrt{1-x^2}-2\int\sqrt{1-x^2}d\sin^{-1}x\\
=x(\sin^{-1}x)^2+2\sin^{-1}x\sqrt{1-x^2}-2x+C
$$

## 63 $\int\sec^3xdx$

$$
=\int\sec xd\tan x\\
=\sec x\tan x-\int\tan xd\sec x\\
=\sec x\tan x-\int \tan^2x\sec xdx\\
=\sec x\tan x-\int (\sec^2x-1)\sec xdx\\
=\sec x\tan x-\int\sec^3xdx+\ln|\sec x+\tan x|\\
integral=\frac{1}{2}(\sec x\tan x+\ln|\sec x+\tan x|)+C
$$

## 64 $\int^{\frac{\pi}{3} }_{\frac{\pi}{4} }\frac{\tan x}{\ln(\cos x)}dx$

$$
=-\int^{\frac{\pi}{3}}_{\frac{\pi}{4}}\frac{\frac{1}{\cos x}}{\ln(\cos x)}d\cos x\\
=-\ln|\ln (\cos x)|\big|^{\frac{\pi}{3}}_{\frac{\pi}{4}}\\
=\ln|\frac{\ln(\cos \frac{\pi}{4})}{\ln(\cos \frac{\pi}{3})}|\\
=\ln|\frac{\ln\frac{\sqrt{2}}{2}}{\ln\frac{1}{2}}|\\
=-\ln2
$$

## 65 $\int^{1}_{-1}\frac{1}{1+x^2}dx$

$$
=\tan^{-1}x\big|^{1}_{-1}\\
=\frac{\pi}{2}
$$

## 66 $\int\frac{1}{2+\sin x}dx$

$$
\text{let }t=\tan \frac{x}{2},x=2\tan^{-1}t\\
integral=\int\frac{1}{2+\frac{2t}{1+t^2}}d2\tan^{-1}t\\
=\int\frac{1}{t^2+t+1}dt\\
=\int\frac{1}{(t+\frac{1}{2})^2+(\frac{\sqrt{3}}{2})^2}dt\\
=\frac{2}{\sqrt{3}}\tan^{-1}\frac{2t+1}{\sqrt{3}}+C\\
=\frac{2}{\sqrt{3}}\tan^{-1}\frac{2\tan\frac{x}{2}+1}{\sqrt{3}}+C
$$

## 67 $\int^{\pi}_{0}\sqrt{\sin^3x-\sin^5x}dx$

$$
=\int^{\pi}_{0}\sin^{\frac{3}{2}}x\sqrt{\cos^2x}dx\\
=-\int^{\pi}_{\frac{\pi}{2}}\sin^{\frac{3}{2}}x\cos xdx+\int^{\frac{\pi}{2}}_{0}\sin^{\frac{3}{2}}x\cos xdx\\
=\frac{2}{5}\sin^{\frac{5}{2}}x\big|^{\pi}_{\frac{\pi}{2}}-\frac{2}{5}\sin^{\frac{5}{2}}x\big|^{\frac{\pi}{2}}_{0}\\
=-\frac{4}{5}
$$

## 68 $\int^{\frac{\pi}{2} }_{0}\frac{\sin x}{\sin x+\cos x}dx$

$$
=\int\frac{1}{1+\cot x}dx\\
\text{let }t=\cot x,x=\cot^{-1}t\\
integral=\int\frac{1}{1+t}d\cot^{-1} t\\
=-\int\frac{1}{(1+t)(1+t^2)}dt\\
=-\int(\frac{A}{1+t}+\frac{Bt+C}{1+t^2})dt\\
=-\int\frac{(A+B)t^2+(B+C)t+A+C}{(1+t)(1+t^2)}\\
\begin{cases}
A+B=0\\
B+C=0\\
A+C=1\\
\end{cases}
\Rightarrow
\begin{cases}
A=\frac{1}{2}\\
B=-\frac{1}{2}\\
C=\frac{1}{2}\\
\end{cases}\\
integral=-\frac{1}{2}\int(\frac{1}{1+t}-\frac{t-1}{t^2+1})dt\\
=-\frac{1}{2}\ln|t+1|+\frac{1}{4}\int\frac{2t}{t^2+1}dt-\frac{1}{2}\int\frac{1}{t^2+1}dt\\
=-\frac{1}{2}\ln|t+1|+\frac{1}{4}\ln|t^2+1|-\frac{1}{2}\tan^{-1} t+C\\
=-\frac{1}{2}\ln|\cot x+1|+\frac{1}{4}\ln|\cot^2x + 1|-\frac{1}{2}\tan^{-1}\cot x+C\\
=-\frac{1}{2}\ln|\cot x+1|+\frac{1}{2}|\csc x|-\frac{1}{2}(\frac{\pi}{2}-x)+C\\
=\frac{1}{2}\ln|\frac{\csc}{\cot x+1}|+\frac{x}{2}+C\\
=\frac{1}{2}\ln|\frac{1}{\cos x+\sin x}|+\frac{x}{2}+C\\
=\frac{1}{2}\ln|\sin x+\cos x|+\frac{x}{2}+C\\
\int^{\frac{\pi}{2}}_{0}\frac{\sin x}{\sin x+\cos x}dx=\frac{\pi}{4}
$$

## 69 $\int^{\frac{\pi}{2} }_{0}e^x\sin xdx$

$$
=\int^{\frac{\pi}{2}}_{0}\sin xde^x\\
=\sin xe^x\big|^{\frac{\pi}{2}}_{0}-\int^{\frac{\pi}{2}}_{0}e^x\cos xdx\\
=e^\frac{\pi}{2}-\int^{\frac{\pi}{2}}_{0}\cos xde^x\\
=e^\frac{\pi}{2}-\cos xe^x\big|^{\frac{\pi}{2}}_{0}-\int^{\frac{\pi}{2}}_{0}\sin xde^x\\
integral=\frac{1}{2}(e^\frac{\pi}{2}-1)
$$

## 70 $\int\frac{x^3}{x^8-2}dx$

$$
=\frac{1}{4}\int \frac{1}{x^8-2}dx^4\\
\text{let }t=x^4\\
integral=\frac{1}{4}\int\frac{1}{t^2-2}dt\\
=\frac{1}{4}\frac{1}{2\sqrt{2}}\ln|\frac{t-\sqrt{2}}{t+\sqrt{2}}|+C\\
=\frac{1}{8\sqrt{2}}\ln|\frac{x^4-\sqrt{2}}{x^4+\sqrt{2}}|+C
$$

## 71 $\int \tan^{-1}xdx$

$$
=x\tan^{-1}x-\int xd\tan^{-1}x\\
=x\tan^{-1}x-\int\frac{x}{1+x^2}dx\\
=x\tan^{-1}x-\frac{1}{2}\ln|1+x^2|+C
$$

## 72 $\int\frac{x^{2n-1} }{x^n+1}dx$

$$
=\frac{1}{2n}\int\frac{1}{x^n+1}dx^{2n}\\
\text{let }t=x^n\\
integral=\frac{1}{2n}\int\frac{1}{t+1}dt^2\\
=\frac{1}{2n}\int\frac{2t+2-2}{t+1}dt\\
=\frac{1}{n}t-\frac{1}{n}\ln|t+1|+C\\
=\frac{1}{n}x^n-\ln|x^n+1|+C
$$

## 73 $\int^{\frac{3\pi}{4} }_{0}\frac{1}{\sin^2x+1}dx$

$$
=\int\frac{1}{\frac{1-\cos2x}{2}+1}dx\\
=2\int\frac{1}{3-\cos2x}dx\\
\text{let }t=\tan x,x=\tan^{-1}t\\
integral=2\int\frac{1}{3-\frac{1-t^2}{1+t^2}}\tan^{-1}t\\
=2\int\frac{1}{4t^2+2}dt\\
=\frac{1}{\sqrt{2}}\int\frac{1}{(\sqrt{2t})^2+1}d\sqrt{2}t\\
=\frac{1}{\sqrt{2}}\tan^{-1}(\sqrt{2t})\\
=\frac{1}{\sqrt{2}}\tan^{-1}(\sqrt{2}\tan x)
$$

## A $\sum^{\infty}_{n=1}\int^{n+1}_{n}2^{-\sqrt{x} }dx$

$$
=\int^{\infty}_{1}2^{-\sqrt{x}}dx\\
\text{let }t=-\sqrt{x},x=t^2,t\le-1\\
integral=\int^{-1}_{-\infty}2^tdt^2\\
=\int^{-1}_{-\infty}2td\frac{2^t}{\ln2}\\
=\frac{t2^{t+1}}{\ln2}\big|^{-1}_{-\infty}-\frac{2}{\ln2}\int^{-1}_{-\infty}2^tdt\\
=-\frac{1}{\ln2}-\frac{2}{\ln2}\cdot\frac{2^t}{\ln2}\big|^{-1}_{-\infty}\\
=-\frac{1}{\ln2}+\frac{1}{\ln^22}
$$

## B $\int^{\infty}_{0}(\sqrt{x^2+1}-x)^2dx$

$$
\text{let }t=\sqrt{x^2+1}-x,\frac{1}{t}=\sqrt{x^2+1}+x,0<t\le1\\
x=\frac{1}{2}(\frac{1}{t}-t)\\
integral=\int^{0}_{1} t^2\frac{1}{2}(-\frac{1}{t^2}-1)dt\\
=\frac{1}{2}\int t^2+1dt\\
=\frac{1}{2}(\frac{1}{3}t^3+t)\big|^{1}_{0}\\
=\frac{2}{3}
$$

## C $\int\ln(1+\sqrt{\frac{x+1}{x} })dx,x>0$

$$
=\int(\ln(\sqrt{x}+\sqrt{x+1})-\ln\sqrt{x})dx\\
=\int\ln(\sqrt{x}+\sqrt{x+1})-\frac{1}{2}\int\ln xdx\\
=\int\ln(\sqrt{x}+\sqrt{x+1})dx-\frac{x}{2}(\ln x-1)\\
\text{let }t=\sqrt{x}+\sqrt{x+1},\frac{1}{t}=\sqrt{x+1}-\sqrt{x},x=(\frac{t-\frac{1}{t}}{2})^2\\
=\frac{1}{4}\int\ln td(t-\frac{1}{t})^2-\frac{x}{2}(\ln x-1)\\
=\frac{1}{4}\ln t(t-\frac{1}{t})^2 -\frac{1}{4}\int(t-\frac{1}{t})^2d\ln t-\frac{x}{2}(\ln x-1)\\
=\frac{2x+1}{2}\ln(\sqrt{x}+\sqrt{x+1})-\frac{1}{2}\sqrt{x^2+x}-\frac{x}{2}(\ln x-1)+C
$$

## D power series $\sum^{\infty}_{n=0}a_nx^n$ converges on $(-\infty,+\infty)$ and its sum function y(x) satisifies $y^{(2)}-2xy^{(1)}-4y=0,y(0)=0,y^{(1)}(0)=1$

### 1 justify $a_{n+2}=\frac{2}{1+n}a_n,n=1,2,\cdots$

### 2 equation for y(x) is

## E

$$
\int^{1}_{0}[\int^{f(x)}_{0}\phi(t)dt]dx=2\int^{1}_{0}xf(x)dx,
\text{where } \phi(x) \text{is the inverse function of }f(x) \text{and } f(1)=0
$$
