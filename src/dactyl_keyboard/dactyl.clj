(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 4)
(def ncols 6)

(def column-curvature (deg2rad 17))                         ; 15                        ; curvature of the columns
(def row-curvature (deg2rad 6))                             ; 5                   ; curvature of the rows
(def centerrow 1.75)                              ; controls front-back tilt
(def centercol 3)                                           ; controls left-right tilt / tenting (higher number is more tenting)
(def tenting-angle (deg2rad 15))                            ; or, change this for more precise tenting control
(def column-style
  (if (> nrows 5) :orthographic :standard))
(defn column-offset [column] (cond
                               (= column 2) [0 5 -3]
                               (= column 3) [0 0 -0.5]
                               (>= column 4) [0 -10 6]
                               :else [0 0 0]))

(def thumb-offsets [10 -5 1])

(def keyboard-z-offset 7)                                   ; controls overall height; original=9 with centercol=3; use 16 for centercol=2
(def bottom-height 2)                                    ; plexiglass plate or printed plate
(def extra-width 3)                                       ; extra space between the base of keys; original= 2
(def extra-height -0.5)                                      ; original= 0.5

(def wall-z-offset -1)                                      ; -5                ; original=-15 length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 1)

(def wall-thickness 2)                                      ; wall thickness parameter; originally 5

; If you use Cherry MX or Gateron switches, this can be turned on.
; If you use other switches such as Kailh, you should set this as false
(def create-side-nubs? false)

;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 14)                                   ;; Was 14.1, then 14.25
(def keyswitch-width 14)
(def plate-thickness 2)
(def keyswitch-below-plate (- 8 plate-thickness))           ; approx space needed below keyswitch

(def sa-profile-key-height 12.7)

(def side-nub-thickness 4)
(def retention-tab-thickness 1.5)
(def retention-tab-hole-thickness (- plate-thickness retention-tab-thickness))
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

;for the bottom
(def filled-plate
  (->> (cube mount-height mount-width plate-thickness)
       (translate [0 0 (/ plate-thickness 2)])
       ))
(def single-plate
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ pi 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 side-nub-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ side-nub-thickness 2)])))
                      (translate [0 0 (- plate-thickness side-nub-thickness)]))
        plate-half (union top-wall left-wall (if create-side-nubs? (with-fn 100 side-nub)))
        top-nub (->> (cube 5 5 retention-tab-hole-thickness)
                     (translate [(+ (/ keyswitch-width 2)) 0 (/ retention-tab-hole-thickness 2)]))
        top-nub-quad (union top-nub
                            (rotate (deg2rad 90) [0 0 1] top-nub)
                            (rotate (deg2rad 180) [0 0 1] top-nub)
                            (rotate (deg2rad 270) [0 0 1] top-nub))]
    (difference
      (union plate-half
             (->> plate-half
                  (mirror [1 0 0])
                  (mirror [0 1 0])))
      top-nub-quad)))

;amoeba is 16 mm high
(def switch-bottom
  (translate [0 0 (/ keyswitch-below-plate -2)] (cube 16 keyswitch-width keyswitch-below-plate)))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(def sa-cap {1   (let [bl2 (/ 18.5 2)
                       m (/ 17 2)
                       key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 0.05]))
                                     (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 6]))
                                     (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 12])))]
                   (->> key-cap
                        (translate [0 0 (+ 5 plate-thickness)])
                        (color [220/255 163/255 163/255 1])))
             2   (let [bl2 sa-length
                       bw2 (/ 18.25 2)
                       key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 0.05]))
                                     (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 12])))]
                   (->> key-cap
                        (translate [0 0 (+ 5 plate-thickness)])
                        (color [127/255 159/255 127/255 1])))
             1.5 (let [bl2 (/ 18.25 2)
                       bw2 (/ 27.94 2)
                       key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 0.05]))
                                     (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 12])))]
                   (->> key-cap
                        (translate [0 0 (+ 5 plate-thickness)])
                        (color [240/255 223/255 175/255 1])))})

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def columns (range 0 ncols))
(def rows (range 0 nrows))

(def cap-top-height (+ plate-thickness sa-profile-key-height))
(def row-radius (+ (/ (/ (+ mount-height extra-height) 2)
                      (Math/sin (/ column-curvature 2)))
                   cap-top-height))
(def column-radius (+ (/ (/ (+ mount-width extra-width) 2)
                         (Math/sin (/ row-curvature 2)))
                      cap-top-height))
(def column-x-delta (+ -1 (- (* column-radius (Math/sin row-curvature)))))

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [column-angle (* row-curvature (- centercol column))
        placed-shape (->> shape
                          (translate-fn [0 0 (- row-radius)])
                          (rotate-x-fn (* column-curvature (- centerrow row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-shape-ortho (->> shape
                                (translate-fn [0 0 (- row-radius)])
                                (rotate-x-fn (* column-curvature (- centerrow row)))
                                (translate-fn [0 0 row-radius])
                                (rotate-y-fn column-angle)
                                (translate-fn [(- (* (- column centercol) column-x-delta)) 0 column-z-delta])
                                (translate-fn (column-offset column)))]

    (->> (case column-style
           :orthographic placed-shape-ortho
           placed-shape)
         (rotate-y-fn tenting-angle)
         (translate-fn [0 0 keyboard-z-offset]))))

(defn key-place [column row shape]
  (apply-key-geometry translate
                      (fn [angle obj] (rotate angle [1 0 0] obj))
                      (fn [angle obj] (rotate angle [0 1 0] obj))
                      column row shape))

(defn rotate-around-x [angle position]
  (mmul
    [[1 0 0]
     [0 (Math/cos angle) (- (Math/sin angle))]
     [0 (Math/sin angle) (Math/cos angle)]]
    position))

(defn rotate-around-y [angle position]
  (mmul
    [[(Math/cos angle) 0 (Math/sin angle)]
     [0 1 0]
     [(- (Math/sin angle)) 0 (Math/cos angle)]]
    position))


(defn rotate-around-z [angle position]
  (mmul
    [[(Math/cos angle) (- (Math/sin angle)) 0]
     [(Math/sin angle) (Math/cos angle) 0]
     [0 0 1]]
    position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

(defn key-places [shape]
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> shape
                (key-place column row)))))
(def key-holes
  (key-places single-plate))
(def key-fills
  (key-places filled-plate))
(def key-space-below
  (key-places switch-bottom))
(def caps
  (key-places (sa-cap 1)))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

; posts are located at the inside corners of the key plates.
; the 'web' is the fill between key plates.
;

(def web-thickness 2)
(def post-size 0.1)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ mount-width 2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height 2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width 2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))

; fat web post for very steep angles between thumb and finger clusters
; this ensures the walls stay somewhat thicker
(def fat-post-size 1.2)
(def fat-web-post (->> (cube fat-post-size fat-post-size web-thickness)
                       (translate [0 0 (+ (/ web-thickness -2)
                                          plate-thickness)])))

(def fat-post-adj (/ fat-post-size 2))
(def fat-web-post-tr (translate [(- (/ mount-width 2) fat-post-adj) (- (/ mount-height 2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-tl (translate [(+ (/ mount-width -2) fat-post-adj) (- (/ mount-height 2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-bl (translate [(+ (/ mount-width -2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-br (translate [(- (/ mount-width 2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
; wide posts for 1.5u keys in the main cluster

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(defn piramid-hulls [top & shapes]
  (apply union
         (map (partial apply hull top)
              (partition 2 1 shapes))))

(def connectors
  (apply union
         (concat
           ;; Row connections
           (for [column (range 0 (dec ncols))
                 row (range 0 lastrow)]
             (triangle-hulls
               (key-place (inc column) row web-post-tl)
               (key-place column row web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place column row web-post-br)))

           ;; Column connections
           (for [column columns
                 row (range 0 cornerrow)]
             (triangle-hulls
               (key-place column row web-post-bl)
               (key-place column row web-post-br)
               (key-place column (inc row) web-post-tl)
               (key-place column (inc row) web-post-tr)))

           ;; Diagonal connections
           (for [column (range 0 (dec ncols))
                 row (range 0 cornerrow)]
             (triangle-hulls
               (key-place column row web-post-br)
               (key-place column (inc row) web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place (inc column) (inc row) web-post-tl))))))

;;;;;;;;;;;;
;; Thumbs ;;
;;;;;;;;;;;;

(def thumborigin
  (map + (key-position 1 cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0])
       thumb-offsets))

(defn thumb-place [rot move shape]
  (->> shape
       (rotate (deg2rad (nth rot 0)) [1 0 0])
       (rotate (deg2rad (nth rot 1)) [0 1 0])
       (rotate (deg2rad (nth rot 2)) [0 0 1])               ; original 10
       (translate thumborigin)
       (translate move)))

; convexer
(defn thumb-r-place [shape] (thumb-place [14 -40 10] [-15 -10 5] shape)) ; right
(defn thumb-m-place [shape] (thumb-place [10 -23 20] [-33 -15 -6] shape)) ; middle
(defn thumb-l-place [shape] (thumb-place [6 -5 35] [-52.5 -25.5 -11.5] shape)) ; left

(defn thumb-layout [shape]
  (union
    (thumb-r-place shape)
    (thumb-m-place shape)
    (thumb-l-place shape)
    ))

(defn debug [shape]
  (color [0.5 0.5 0.5 0.5] shape))

(def thumbcaps (thumb-layout (sa-cap 1)))
(def thumb (thumb-layout single-plate))
(def thumb-fill (thumb-layout filled-plate))
(def thumb-space-below (thumb-layout switch-bottom))
;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(defn wall-locate1 [dx dy] [(* dx wall-thickness) (* dy wall-thickness) 0])
(defn wall-locate2 [dx dy] [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
(defn wall-locate3 [dx dy] [(* dx (+ wall-xy-offset wall-thickness)) (* dy (+ wall-xy-offset wall-thickness)) wall-z-offset])

(def thumb-connectors
  (union
    (triangle-hulls                                         ; top two
      (thumb-m-place web-post-tr)
      (thumb-m-place web-post-br)
      (thumb-r-place web-post-tl)
      (thumb-r-place web-post-bl))
    (triangle-hulls                                         ; top two
      (thumb-m-place web-post-tl)
      (thumb-l-place web-post-tr)
      (thumb-m-place web-post-bl)
      (thumb-l-place web-post-br)
      (thumb-m-place web-post-bl))
    (triangle-hulls                                         ; top two to the main keyboard, starting on the left
      (key-place 2 lastrow web-post-br)
      (key-place 3 lastrow web-post-bl)
      (key-place 2 lastrow web-post-tr)
      (key-place 3 lastrow web-post-tl)
      (key-place 3 cornerrow web-post-bl)
      (key-place 3 lastrow web-post-tr)
      (key-place 3 cornerrow web-post-br)
      (key-place 4 cornerrow web-post-bl))
    (triangle-hulls
      (key-place 1 cornerrow web-post-br)
      (key-place 2 lastrow web-post-tl)
      (key-place 2 cornerrow web-post-bl)
      (key-place 2 lastrow web-post-tr)
      (key-place 2 cornerrow web-post-br)
      (key-place 3 cornerrow web-post-bl))
    (triangle-hulls
      (key-place 3 lastrow web-post-tr)
      (key-place 3 lastrow web-post-br)
      (key-place 3 lastrow web-post-tr)
      (key-place 4 cornerrow web-post-bl))
    (hull                                                   ; between thumb m and top key
      (key-place 0 cornerrow (translate (wall-locate1 -1 0) web-post-bl))
      (thumb-m-place web-post-tr)
      (thumb-m-place web-post-tl))
    (piramid-hulls                                          ; top ridge thumb side
      (key-place 0 cornerrow (translate (wall-locate1 -1 0) fat-web-post-bl))
      (key-place 0 cornerrow (translate (wall-locate2 -1 0) web-post-bl))
      (key-place 0 cornerrow web-post-bl)
      ;(thumb-r-place web-post-tr)
      (thumb-r-place web-post-tl)
      (thumb-m-place web-post-tr)
      (key-place 0 cornerrow (translate (wall-locate3 -1 0) web-post-bl))
      )
    (triangle-hulls
      (key-place 0 cornerrow fat-web-post-br)
      (key-place 0 cornerrow fat-web-post-bl)
      (thumb-r-place web-post-tl)
      (key-place 1 cornerrow web-post-bl)
      (key-place 1 cornerrow web-post-br)
      )
    (triangle-hulls
      (thumb-r-place fat-web-post-tl)
      (thumb-r-place fat-web-post-tr)
      (key-place 1 cornerrow web-post-br)
      (key-place 2 lastrow web-post-tl)
      )
    (triangle-hulls
      (key-place 2 lastrow web-post-tl)
      (thumb-r-place fat-web-post-tr)
      (key-place 2 lastrow web-post-bl)
      (thumb-r-place fat-web-post-br)
      )
    (triangle-hulls
      (thumb-r-place web-post-br)
      (key-place 2 lastrow web-post-bl)
      (key-place 3 lastrow web-post-bl)
      (key-place 2 lastrow web-post-br)
      )
    ))

; dx1, dy1, dx2, dy2 = direction of the wall. '1' for front, '-1' for back, '0' for 'not in this direction'.
; place1, place2 = function that places an object at a location, typically refers to the center of a key position.
; post1, post2 = the shape that should be rendered
(defn wall-brace [place1 dx1 dy1 post1 place2 dx2 dy2 post2]
  (union
    (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1) post1))
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place1 (translate (wall-locate3 dx1 dy1) post1))
      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2) post2))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      (place2 (translate (wall-locate3 dx2 dy2) post2)))
    (bottom-hull
      (place1 (translate (wall-locate2 dx1 dy1) post1))
      (place1 (translate (wall-locate3 dx1 dy1) post1))
      (place2 (translate (wall-locate2 dx2 dy2) post2))
      (place2 (translate (wall-locate3 dx2 dy2) post2)))))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 x2 y2 dx2 dy2 post2]
  (wall-brace (partial key-place x1 y1) dx1 dy1 post1
              (partial key-place x2 y2) dx2 dy2 post2))

(defn key-corner [x y loc]
  (case loc
    :tl (key-wall-brace x y 0 1 web-post-tl x y -1 0 web-post-tl)
    :tr (key-wall-brace x y 0 1 web-post-tr x y 1 0 web-post-tr)
    :bl (key-wall-brace x y 0 -1 web-post-bl x y -1 0 web-post-bl)
    :br (key-wall-brace x y 0 -1 web-post-br x y 1 0 web-post-br)))

(def right-wall
  (union (key-corner lastcol 0 :tr)
         (for [y (range 0 lastrow)] (key-wall-brace lastcol y 1 0 web-post-tr lastcol y 1 0 web-post-br))
         (for [y (range 1 lastrow)] (key-wall-brace lastcol (dec y) 1 0 web-post-br lastcol y 1 0 web-post-tr))
         (key-corner lastcol cornerrow :br)))


(def case-walls
  (union
    right-wall
    ; back wall
    (for [x (range 0 ncols)] (key-wall-brace x 0 0 1 web-post-tl x 0 0 1 web-post-tr))
    (for [x (range 1 ncols)] (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))
    ; left wall
    (for [y (range 0 lastrow)] (key-wall-brace 0 y -1 0 web-post-tl 0 y -1 0 web-post-bl))
    (for [y (range 1 lastrow)] (key-wall-brace 0 (dec y) -1 0 web-post-bl 0 y -1 0 web-post-tl))
    (wall-brace (partial key-place 0 cornerrow) -1 0 web-post-bl thumb-m-place 0 1 web-post-tl)
    ; left-back-corner
    (key-wall-brace 0 0 0 1 web-post-tl 0 0 -1 0 web-post-tl)
    ; front wall
    (key-wall-brace 3 lastrow 0 -1 web-post-bl 3 lastrow 0.5 -1 web-post-br)
    (key-wall-brace 3 lastrow 0.5 -1 web-post-br 4 cornerrow 0.5 -1 web-post-bl)
    (for [x (range 4 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl x cornerrow 0 -1 web-post-br)) ; TODO fix extra wall
    (for [x (range 5 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl (dec x) cornerrow 0 -1 web-post-br))
    (wall-brace thumb-r-place 0 -1 web-post-br (partial key-place 3 lastrow) 0 -1 web-post-bl)
    ; thumb walls
    (wall-brace thumb-r-place 0 -1 web-post-br thumb-r-place 0 -1 web-post-bl)
    (wall-brace thumb-m-place 0 -1 web-post-br thumb-m-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place 0 -1 web-post-br thumb-l-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place 0 1 web-post-tr thumb-l-place 0 1 web-post-tl)
    (wall-brace thumb-l-place -1 0 web-post-tl thumb-l-place -1 0 web-post-bl)
    ; thumb corners
    (wall-brace thumb-l-place -1 0 web-post-bl thumb-l-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place -1 0 web-post-tl thumb-l-place 0 1 web-post-tl)
    ; thumb tweeners
    (wall-brace thumb-r-place 0 -1 web-post-bl thumb-m-place 0 -1 web-post-br)
    (wall-brace thumb-m-place 0 -1 web-post-bl thumb-l-place 0 -1 web-post-br)
    (wall-brace thumb-m-place 0 1 web-post-tl thumb-l-place 0 1 web-post-tr)
    (wall-brace thumb-l-place -1 0 web-post-bl thumb-l-place -1 0 web-post-tl)
    ))


; Screw insert definition & position
(defn screw-insert-shape [bottom-radius top-radius height]
  (->> (binding [*fn* 30]
         (cylinder [bottom-radius top-radius] height)))
  )

(defn screw-insert [column row bottom-radius top-radius height offset]
  (let [position (key-position column row [0 0 0])]
    (->> (screw-insert-shape bottom-radius top-radius height)
         (translate (map + offset [(first position) (second position) (/ height 2)])))))

(defn screw-insert-all-shapes [bottom-radius top-radius height]
  (union (screw-insert 2 0 bottom-radius top-radius height [-4 4.5 bottom-height]) ; top middle
         (screw-insert 0 1 bottom-radius top-radius height [-5.3 -8 bottom-height]) ; left
         (screw-insert 0 lastrow bottom-radius top-radius height [-12 -7 bottom-height]) ;thumb
         (screw-insert (- lastcol 1) lastrow bottom-radius top-radius height [10 13.5 bottom-height]) ; bottom right
         (screw-insert (- lastcol 1) 0 bottom-radius top-radius height [10 5 bottom-height]) ; top right
         (screw-insert 2 (+ lastrow 1) bottom-radius top-radius height [0 6.5 bottom-height]))) ;bottom middle

; Hole Depth Y: 4.4
(def screw-insert-height 4)

; Hole Diameter C: 4.1-4.4
(def screw-insert-bottom-radius (/ 4.0 2))
(def screw-insert-top-radius (/ 3.9 2))
(def screw-insert-holes (screw-insert-all-shapes screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))

; Wall Thickness W:\t1.65
(def screw-insert-outers (screw-insert-all-shapes (+ screw-insert-bottom-radius 1.65) (+ screw-insert-top-radius 1.65) (+ screw-insert-height 1.5)))
(def screw-insert-screw-holes (screw-insert-all-shapes 1.7 1.7 350))



(def usb-holder (mirror [-1 0 0]
                    (import "../things/holder v8.stl")))

(def usb-holder (translate [-40.8 45.5 bottom-height] usb-holder))
(def usb-holder-space
  (translate [0 0 (/ (+  bottom-height 8.2) 2)]
  (extrude-linear {:height (+ bottom-height 8.2) :twist 0 :convexity 0}
                  (offset 0.1
                          (project usb-holder)))))

(spit "things/test2.scad" (write-scad usb-holder))


(def model-outline
  (project
    (union
      key-fills
      connectors
      thumb-fill
      thumb-connectors
      case-walls)))

(def model-right
  (difference
    (union
      key-holes
      connectors
      thumb
      thumb-connectors
      (difference (union case-walls
                         screw-insert-outers
                         )
                  usb-holder-space
                  screw-insert-holes
                  ))
    (translate [0 0 -20] (cube 350 350 40))))
;
(spit "things/right.scad"
      (write-scad model-right))
;
;(spit "things/left.scad"
;      (write-scad (mirror [-1 0 0] model-right)))
(spit "things/test.scad"
      (write-scad
        (difference
          (union
            key-holes
            connectors
            thumb
            thumb-connectors
            caps
            thumbcaps
            (difference (union case-walls
                               screw-insert-outers
                               )
                        usb-holder-space
                        screw-insert-holes
                        )
            (debug key-space-below)
            (debug thumb-space-below)
            (debug usb-holder)
            )
          (translate [0 0 -20] (cube 350 350 40)))))

(spit "things/thumb.scad"
      (write-scad
        (difference
          (union
            thumb
            thumb-connectors
            thumbcaps
            )
          (translate [0 0 -20] (cube 350 350 40)))))

(def wall-shape
  (cut
    (translate [0 0 -0.1]
               (union case-walls
                      screw-insert-outers
                      )
               ))
  )

(def wall-shape (cut (translate [0 0 -0.1] (difference case-walls usb-holder-space))))

(def bottom-height-half (/ bottom-height 2))
(def bottom-plate
    (translate [0 0 bottom-height-half] (extrude-linear {:height 2 :twist 0 :convexity 0}
                                                        (difference model-outline
                                                                    (offset 0.1 wall-shape)
                                                                    ))
    ))
(def bottom-wall
  (translate [0 0 bottom-height-half] (extrude-linear {:height bottom-height :twist 0 :convexity 0} wall-shape)))

(def bottom-wall-usb-holder
  (translate [0 0 bottom-height]
             (extrude-linear {:height bottom-height-half :twist 0 :convexity 0}
                             (offset 3 ))))

(def screw-head-height 1)
(def layer-height 0.2)
(def bottom-screw-holes-head
  (translate [0 0 (- bottom-height)] (screw-insert-all-shapes 2.25 2.25 screw-head-height)))
;keep a layer thickness so we can bridge over without supports
(def bottom-screw-holes-top
  (translate [0 0 (- layer-height screw-head-height)]
             (screw-insert-all-shapes 1 1 (- bottom-height screw-head-height))))

;(spit "things/test2.scad" (write-scad (union bottom-screw-holes-head bottom-screw-holes-top) ))
(spit "things/right-plate-print.scad"
      (write-scad
        (difference
          bottom-plate
          (union
            bottom-wall-usb-holder
            key-space-below
            thumb-space-below
            bottom-screw-holes-head
            bottom-screw-holes-top
            ))))

(spit "things/right-plate-cut.scad"
      (write-scad
        (cut
          (translate [0 0 (- bottom-height)]                ;biggest cutout on top
                     (difference
                       (union
                         bottom-plate
                         )
                       (union
                         bottom-wall-usb-holder
                         thumb-space-below
                         (screw-insert-all-shapes 1 1 50)))))))

