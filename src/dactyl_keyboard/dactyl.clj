(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 4)
(def ncols 6)

(def column-curvature (deg2rad 20))                         ; 15                        ; curvature of the columns
(def row-curvature (deg2rad 6))                             ; 5                   ; curvature of the rows

(def centerrow (- nrows 3))             ; controls front-back tilt
(def centercol 2)                       ; controls left-right tilt / tenting (higher number is more tenting)
(def tenting-angle (deg2rad 15))            ; or, change this for more precise tenting control
(def column-style
  (if (> nrows 5) :orthographic :standard))  ; options include :standard, :orthographic, and :fixed
; (def column-style :fixed)
(def pinky-15u false)
(def extra-row false)                   ; adds an extra bottom row to the outer columns
(def inner-column false)                ; adds an extra inner column (two less rows than nrows)

;external case for controller and ports
(def external-controller true)
(def external-controller-height 10)
(def external-controller-step 1.5)
(def external-controller-width 31.666)

; magnet holes for external wrist rest
(def magnet-holes true)
(def magnet-height 2)
(def magnet-booster-width 1)
(def magnet-diameter 10)
(def magnet-wall-width 1)
(def magnet-connector-wall-width 1.5)
(def magnet-connector-length 4)
(def magnet-inner-diameter 3)

; If you want hot swap sockets enable this
(def hot-swap false)
(def low-profile true)
(def plate-height 2)
(def plate-border-height 1)
(defn column-offset [column] (cond
                               (= column 2) [0 2.82 -4.5]
                               (>= column 4) [0 -19 5.64]   ; pinky finger
                               (< column 2) [0 -5.8 3]      ; index finger
                               :else [0 0 0]))              ; ring finger

(def thumb-offsets [6 -3 7])

(def keyboard-z-offset
  8 ;(if hot-swap 12 9)
  )               ; controls overall height; original=9 with centercol=3; use 16 for centercol=2

(def extra-width 2.5)                   ; extra space between the base of keys; original= 2
(def extra-height 1.0)                  ; original= 0.5

(def wall-z-offset -5)                 ; original=-15 length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 5)                  ; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-thickness 2)                  ; wall thickness parameter; originally 5

;; Settings for column-style == :fixed
;; The defaults roughly match Maltron settings
;;   http://patentimages.storage.googleapis.com/EP0219944A2/imgf0002.png
;; Fixed-z overrides the z portion of the column ofsets above.
;; NOTE: THIS DOESN'T WORK QUITE LIKE I'D HOPED.
(def fixed-angles [(deg2rad 10) (deg2rad 10) 0 0 0 (deg2rad -15) (deg2rad -15)])
(def fixed-x [-41.5 -22.5 0 20.3 41.4 65.5 89.6])  ; relative to the middle finger
(def fixed-z [12.1    8.3 0  5   10.7 14.5 17.5])
(def fixed-tenting (deg2rad 0))

; If you use Cherry MX or Gateron switches, this can be turned on.
; If you use other switches such as Kailh, you should set this as false
(def create-side-nubs? false)

; Holes inside keyswitch
(def use-top-nub false)

;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 14.2) ;; Was 14.1, then 14.25
(def keyswitch-width 14.2)

(def sa-profile-key-height 12.7)

(def plate-thickness
  (if low-profile
    3
    4
    )) ; толщина верхних стенок

(def side-nub-thickness 4)
(def retention-tab-thickness 1.5)
(def retention-tab-hole-thickness (- plate-thickness retention-tab-thickness))
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

;;;;;;;;;;;;;;
;; Hot swap ;;
;;;;;;;;;;;;;;

(def socket-height-adjust 1.2)
(def hot-swap-diameter 3.1)
(def hot-swap-vertical-offset -1)
(def hot-swap-radius (/ hot-swap-diameter 2))

(def hot-swap-hole-position-x 2.475)
(def hot-swap-hole-position-y 0.325)
(def hot-swap-hole-offset-y 2.20)
(def hot-swap-hole-offset-x 5)

;; low profile
(def hot-swap-low-profile-height 1.85)
(def hot-swap-low-profile-half-width 4.65)
(def hot-swap-low-profile-length 9.55)
(def hot-swap-low-profile-half-length 4.75)

(def hot-socket-low-profile
  (difference
   ; hot-swap plate
   (difference
    (translate [0 0 (- hot-swap-vertical-offset (/ socket-height-adjust 2)) ]
               (cube (+ keyswitch-height 3.6) (+ keyswitch-width 3) (+ plate-thickness socket-height-adjust))
               )
    (translate [0 0 (- (/ socket-height-adjust -2) -0.5)]
               (cube keyswitch-height keyswitch-width (+ socket-height-adjust plate-thickness))
               )
    )

   ; corner hot-fix for tightly
   (rotate [0 (deg2rad 25) (deg2rad 40)]
           (translate [16.5 3 5]
                      (cube 10 10 10)
                      )
           )

   ; hot-swap socket hole
   (scale [1 1 1]
          ; keyboard center hole
          (binding [*fn* 100] (cylinder 2.5 20))

          ; socket connector 1 hole
          (translate [4.4 4.7 0]
                     (binding [*fn* 200] (cylinder hot-swap-radius 20))
                     )

          ; socket connector 2 hole
          (translate [-2.6 5.75 0] ; -3.875 -2.215
                     (binding [*fn* 200] (cylinder hot-swap-radius 20))
                     )
          )
   ;half hole
   (translate [0 (/ (+ keyswitch-width 3) -4) (- -2.05 socket-height-adjust) ]
              (cube (+ keyswitch-height 3.6) (/ (+ keyswitch-width 3) 2) 3.1)
              )
   ;(binding [*fn* 50] (cylinder 2 2))
   )

  )

(def hot-socket-standart
  (difference
   ; hot-swap plate
   (difference
    (translate [0 0 (- hot-swap-vertical-offset (/ socket-height-adjust 2))]
               (cube (+ keyswitch-height 3.6) (+ keyswitch-width 3) (+ plate-thickness socket-height-adjust))
               )
    (translate [0 0 (- (/ socket-height-adjust -2) -0.5)]
               (cube keyswitch-height keyswitch-width (+ socket-height-adjust 2.2))
               )
    )

   ; corner hot-fix for tightly
   (rotate [0 (deg2rad 25) (deg2rad 40)]
           (translate [16.5 3 5]
                      (cube 10 10 10)
                      )
           )

   ; hot-swap socket hole
   (scale [1 1 1]
          (translate [0.075 4.815 (- -2.75 socket-height-adjust)]
                     (union
                      (translate [2.475 0.325 0]
                                 (binding [*fn* 200] (cylinder hot-swap-radius 20))
                                 )
                      (translate [-3.875 -2.215 0]
                                 (binding [*fn* 200] (cylinder hot-swap-radius 20))
                                 )
                      )
                     )
          ; keyboard center hole
          (binding [*fn* 100] (cylinder 2.3 20))
          )
   ;half hole
   (translate [0 (/ (+ keyswitch-width 3) -4) (- -2.05 socket-height-adjust) ]
              (cube (+ keyswitch-height 3.6) (/ (+ keyswitch-width 3) 2) 3.1)
              )
   ;(binding [*fn* 50] (cylinder 2 2))
   )
  )

(def hot-socket
  (if low-profile
    hot-socket-low-profile
    hot-socket-standart
    )
  )

(def nub-size
  (if use-top-nub 5 0)
  )

(def single-plate-right
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 side-nub-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ side-nub-thickness 2)])))
                      (translate [0 0 (- plate-thickness side-nub-thickness)]))
        plate-half (union top-wall left-wall (if create-side-nubs? (with-fn 100 side-nub)))
        top-nub (->> (cube nub-size nub-size  retention-tab-hole-thickness)
                     (translate [(+ (/ keyswitch-width 2)) 0 (/ retention-tab-hole-thickness 2)]))
        top-nub-pair (union top-nub
                            (->> top-nub
                                 (mirror [1 0 0])
                                 (mirror [0 1 0])))]
    (difference
     (union plate-half
            (->> plate-half
                 (mirror [1 0 0])
                 (mirror [0 1 0]))
            (if hot-swap (mirror [0 0 0] hot-socket))
            )
     (->>
      top-nub-pair
      (rotate (/ π 2) [0 0 1])))))

(def single-plate-left
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 side-nub-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ side-nub-thickness 2)])))
                      (translate [0 0 (- plate-thickness side-nub-thickness)]))
        plate-half (union top-wall left-wall (if create-side-nubs? (with-fn 100 side-nub)))
        top-nub (->> (cube nub-size nub-size retention-tab-hole-thickness)
                     (translate [(+ (/ keyswitch-width 2)) 0 (/ retention-tab-hole-thickness 2)]))
        top-nub-pair (union top-nub
                            (->> top-nub
                                 (mirror [1 0 0])
                                 (mirror [0 1 0])))]
    (difference
     (union plate-half
            (->> plate-half
                 (mirror [1 0 0])
                 (mirror [0 1 0]))
            (if hot-swap (mirror [1 0 0] hot-socket))
            )
     (->>
      top-nub-pair
      (rotate (/ π 2) [0 0 1])))))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(def sa-cap {1 (let [bl2 (/ 18.5 2)
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
             2 (let [bl2 sa-length
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

(defn offset-for-column [col]
  (if (and (true? pinky-15u) (= col lastcol)) 5.5 0))
(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [column-angle (* row-curvature (- centercol column))
        placed-shape (->> shape
                          (translate-fn [(offset-for-column column) 0 (- row-radius)])
                          (rotate-x-fn  (* column-curvature (- centerrow row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn  column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-shape-ortho (->> shape
                                (translate-fn [0 0 (- row-radius)])
                                (rotate-x-fn  (* column-curvature (- centerrow row)))
                                (translate-fn [0 0 row-radius])
                                (rotate-y-fn  column-angle)
                                (translate-fn [(- (* (- column centercol) column-x-delta)) 0 column-z-delta])
                                (translate-fn (column-offset column)))
        placed-shape-fixed (->> shape
                                (rotate-y-fn  (nth fixed-angles column))
                                (translate-fn [(nth fixed-x column) 0 (nth fixed-z column)])
                                (translate-fn [0 0 (- (+ row-radius (nth fixed-z column)))])
                                (rotate-x-fn  (* column-curvature (- centerrow row)))
                                (translate-fn [0 0 (+ row-radius (nth fixed-z column))])
                                (rotate-y-fn  fixed-tenting)
                                (translate-fn [0 (second (column-offset column)) 0]))]
    (->> (case column-style
           :orthographic placed-shape-ortho
           :fixed        placed-shape-fixed
           placed-shape)
         (rotate-y-fn  tenting-angle)
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
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position]
  (mmul
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

(def key-holes-right
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> single-plate-right
                (key-place column row)))))

(def key-holes-left
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> single-plate-left
                (key-place column row)))))
(def caps
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> (sa-cap (if (and (true? pinky-15u) (= column lastcol)) 1.5 1))
                (key-place column row)))))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

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

; wide posts for 1.5u keys in the main cluster

(if (true? pinky-15u)
  (do (def wide-post-tr (translate [(- (/ mount-width 1.2) post-adj)  (- (/ mount-height  2) post-adj) 0] web-post))
    (def wide-post-tl (translate [(+ (/ mount-width -1.2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
    (def wide-post-bl (translate [(+ (/ mount-width -1.2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
    (def wide-post-br (translate [(- (/ mount-width 1.2) post-adj)  (+ (/ mount-height -2) post-adj) 0] web-post)))
  (do (def wide-post-tr web-post-tr)
    (def wide-post-tl web-post-tl)
    (def wide-post-bl web-post-bl)
    (def wide-post-br web-post-br)))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

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

(defn thumb-tr-place [shape]
  (->> shape
       (rotate (deg2rad  14) [1 0 0])
       (rotate (deg2rad -15) [0 1 0])
       (rotate (deg2rad  15) [0 0 1]) ; original 10
       (translate thumborigin)
       (translate [-15 -10 5]))) ; original 1.5u  (translate [-12 -16 3])
(defn thumb-tl-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  25) [0 0 1]) ; original 10
       (translate thumborigin)
       (translate [-35 -16 -2]))) ; original 1.5u (translate [-32 -15 -2])))

(defn thumb-6-place [shape]
  (->> shape
       (rotate (deg2rad  13) [1 0 0])
       (rotate (deg2rad -15) [0 1 0])
       (rotate (deg2rad  15) [0 0 1])
       (translate thumborigin)
       (translate [-6 -28 1])))

(defn thumb-mr-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  25) [0 0 1])
       (translate thumborigin)
       (translate [-24 -35 -6])))
(defn thumb-br-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  35) [0 0 1])
       (translate thumborigin)
       (translate [-39 -43 -16])))
(defn thumb-bl-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -32) [0 1 0])
       (rotate (deg2rad  35) [0 0 1])
       (translate thumborigin)
       (translate [-51 -25 -11.5]))) ;        (translate [-51 -25 -12])))


(defn thumb-1x-layout [shape]
  (union
   (thumb-mr-place shape)
   (thumb-6-place shape)
   (thumb-br-place shape)
   (thumb-tl-place shape)
   (thumb-bl-place shape)))

(defn thumb-15x-layout [shape]
  (union
   (thumb-tr-place shape)))

(def larger-plate
  (let [plate-height (- (/ (- sa-double-length mount-height) 3) 0.5)
        top-plate (->> (cube mount-width plate-height web-thickness)
                       (translate [0 (/ (+ plate-height mount-height) 2)
                                   (- plate-thickness (/ web-thickness 2))]))]
    (union top-plate (mirror [0 1 0] top-plate))))

(def thumbcaps
  (union
   (thumb-1x-layout (sa-cap 1))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1)))))

(def thumb-right
  (union
   (thumb-1x-layout single-plate-right)
   (thumb-15x-layout single-plate-right)
   ; (thumb-15x-layout larger-plate)
   ))

(def thumb-left
  (union
   (thumb-1x-layout single-plate-left)
   (thumb-15x-layout single-plate-left)
   ; (thumb-15x-layout larger-plate)
   ))

(def thumb-post-tr (translate [(- (/ mount-width 2) post-adj)  (- (/ mount-height  2) post-adj) 0] web-post))
(def thumb-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def thumb-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def thumb-post-br (translate [(- (/ mount-width 2) post-adj)  (+ (/ mount-height -2) post-adj) 0] web-post))

(def thumb-connectors
  (union
   (triangle-hulls    ; top two
    (thumb-tl-place web-post-tr)
    (thumb-tl-place web-post-br)
    (thumb-tr-place thumb-post-tl)
    (thumb-tr-place thumb-post-bl))
   (triangle-hulls    ; bottom two
    (thumb-br-place web-post-tr)
    (thumb-br-place web-post-br)
    (thumb-mr-place web-post-tl)
    (thumb-mr-place web-post-bl))
   (triangle-hulls    ; bottom two
    (thumb-mr-place web-post-tr)
    (thumb-mr-place web-post-br)
    (thumb-6-place web-post-tl)
    (thumb-6-place web-post-bl))
   (triangle-hulls    ; between top row and bottom row
    (thumb-br-place web-post-tl)
    (thumb-bl-place web-post-bl)
    (thumb-br-place web-post-tr)
    (thumb-bl-place web-post-br)
    (thumb-mr-place web-post-tl)
    (thumb-tl-place web-post-bl)
    (thumb-mr-place web-post-tr)
    (thumb-tl-place web-post-br)

    (thumb-6-place web-post-tl)
    (thumb-tr-place web-post-bl)
    (thumb-6-place web-post-tr)
    (thumb-tr-place web-post-br)
    )
   (triangle-hulls    ; top two to the middle two, starting on the left
    (thumb-tl-place web-post-tl)
    (thumb-bl-place web-post-tr)
    (thumb-tl-place web-post-bl)
    (thumb-bl-place web-post-br)
    (thumb-mr-place web-post-tr)
    (thumb-tl-place web-post-bl)
    (thumb-tl-place web-post-br)
    (thumb-mr-place web-post-tr))
   (triangle-hulls    ; top two to the middle two, starting on the left
    (key-place 2 lastrow web-post-bl)
    (thumb-6-place thumb-post-tr)
    (key-place 2 lastrow web-post-br)
     )
   (triangle-hulls    ; top two to the main keyboard, starting on the left
    (thumb-tl-place web-post-tl)
    (key-place 0 cornerrow web-post-bl)
    (thumb-tl-place web-post-tr)
    (key-place 0 cornerrow web-post-br)
    (thumb-tr-place thumb-post-tl)
    (key-place 1 cornerrow web-post-bl)
    (thumb-tr-place thumb-post-tr)
    (key-place 1 cornerrow web-post-br)
    (key-place 2 lastrow web-post-tl)
    (key-place 2 lastrow web-post-bl)
    (thumb-tr-place thumb-post-tr)
    (key-place 2 lastrow web-post-bl)
    (thumb-tr-place thumb-post-br)
    (thumb-6-place thumb-post-tr)

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
   ))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(def left-wall-x-offset 5) ; original 10
(def left-wall-z-offset  3) ; original 3

(defn left-key-position [row direction]
  (map - (key-position 0 row [(* mount-width -0.5) (* direction mount-height 0.5) 0]) [left-wall-x-offset 0 left-wall-z-offset]))

(defn left-key-place [row direction shape]
  (translate (left-key-position row direction) shape))

(defn wall-locate1 [dx dy] [(* dx wall-thickness) (* dy wall-thickness) -1])
(defn wall-locate2 [dx dy] [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
(defn wall-locate3 [dx dy] [(* dx (+ wall-xy-offset wall-thickness)) (* dy (+ wall-xy-offset wall-thickness)) wall-z-offset])

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

(def right-wall
  (let [tr (if (true? pinky-15u) wide-post-tr web-post-tr)
        br (if (true? pinky-15u) wide-post-br web-post-br)]
    (union (key-wall-brace lastcol 0 0 1 tr lastcol 0 1 0 tr)
           (for [y (range 0 lastrow)] (key-wall-brace lastcol y 1 0 tr lastcol y 1 0 br))
           (for [y (range 1 lastrow)] (key-wall-brace lastcol (dec y) 1 0 br lastcol y 1 0 tr))
           (key-wall-brace lastcol cornerrow 0 -1 br lastcol cornerrow 1 0 br))))

(def case-walls
  (union
   right-wall
   ; back wall
   (for [x (range 0 ncols)] (key-wall-brace x 0 0 1 web-post-tl x       0 0 1 web-post-tr))
   (for [x (range 1 ncols)] (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))

   ; left wall
   (for [y (range 0 lastrow)] (union (wall-brace (partial left-key-place y 1)       -1 0 web-post (partial left-key-place y -1) -1 0 web-post)
                                     (hull (key-place 0 y web-post-tl)
                                           (key-place 0 y web-post-bl)
                                           (left-key-place y  1 web-post)
                                           (left-key-place y -1 web-post))))
   (for [y (range 1 lastrow)] (union (wall-brace (partial left-key-place (dec y) -1) -1 0 web-post (partial left-key-place y  1) -1 0 web-post)
                                     (hull (key-place 0 y       web-post-tl)
                                           (key-place 0 (dec y) web-post-bl)
                                           (left-key-place y        1 web-post)
                                           (left-key-place (dec y) -1 web-post))))
   (wall-brace (partial key-place 0 0) 0 1 web-post-tl (partial left-key-place 0 1) 0 1 web-post)
   (wall-brace (partial left-key-place 0 1) 0 1 web-post (partial left-key-place 0 1) -1 0 web-post)
   ; front wall
   (key-wall-brace 3 lastrow   0 -1 web-post-bl 3 lastrow 0.5 -1 web-post-br)
   (key-wall-brace 3 lastrow 0.5 -1 web-post-br 4 cornerrow 0.5 -1 web-post-bl)
   (for [x (range 4 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl x       cornerrow 0 -1 web-post-br)) ; TODO fix extra wall
   (for [x (range 5 ncols)] (key-wall-brace x cornerrow 0 -1 web-post-bl (dec x) cornerrow 0 -1 web-post-br))
   ; thumb walls
   (wall-brace thumb-6-place  0 -1 web-post-br thumb-6-place  0 -1 thumb-post-tr)
   (wall-brace thumb-6-place  0 -1 web-post-br thumb-6-place  0 -1 web-post-bl)
   (wall-brace thumb-mr-place  0 -1 web-post-br thumb-mr-place  0 -1 web-post-bl) ; ?
   (wall-brace thumb-br-place  0 -1 web-post-br thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-bl-place  0  1 web-post-tr thumb-bl-place  0  1 web-post-tl)
   (wall-brace thumb-br-place -1  0 web-post-tl thumb-br-place -1  0 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place -1  0 web-post-bl)
   ; thumb corners
   (wall-brace thumb-br-place -1  0 web-post-bl thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place  0  1 web-post-tl)
   ; thumb tweeners
   (wall-brace thumb-mr-place  0 -1 web-post-bl thumb-br-place  0 -1 web-post-br)
   (wall-brace thumb-6-place  0 -1 web-post-bl thumb-mr-place  0 -1 web-post-br)
   (wall-brace thumb-bl-place -1  0 web-post-bl thumb-br-place -1  0 web-post-tl)
   (wall-brace thumb-6-place  0 -1 thumb-post-tr (partial key-place 3 lastrow)  0 -1 web-post-bl)
   ; clunky bit on the top left thumb connection  (normal connectors don't work well)
   (bottom-hull
    (left-key-place cornerrow -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr)))
   (hull
    (left-key-place cornerrow -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (thumb-tl-place web-post-tl)
    )
   (hull
    (left-key-place cornerrow -1 web-post)
    (left-key-place cornerrow -1 (translate (wall-locate1 -1 0) web-post))
    (left-key-place cornerrow -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-tl-place web-post-tl)
    )
   (hull
    (left-key-place cornerrow -1 web-post)
    (left-key-place cornerrow -1 (translate (wall-locate1 -1 0) web-post))
    (key-place 0 cornerrow web-post-bl)
    (thumb-tl-place web-post-tl)
    )
   (hull
    (thumb-bl-place web-post-tr)
    (thumb-bl-place (translate (wall-locate1 -0.3 1) web-post-tr))
    (thumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (thumb-tl-place web-post-tl)
    )
   ))

(defn shape-insert [column row offset shape]
  (let [shift-right   (= column lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (>= row lastrow))
        position      (if shift-up     (key-position column row (map + (wall-locate2  0  1) [0 (/ mount-height 2) 0]))
                        (if shift-down  (key-position column row (map - (wall-locate2  0 -1) [0 (/ mount-height 2) 0]))
                          (if shift-left (map + (left-key-position row 0) (wall-locate3 -1 0))
                            (key-position column row (map + (wall-locate2  1  0) [(/ mount-width 2) 0 0])))))]
    (->> shape (translate (map + offset [(first position) (second position) 0])))))

(defn magnet-shape-insert [column row offset shape]
  (let [shift-right   (= column lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (>= row lastrow))
        position      (if shift-up     (key-position column row (map + (wall-locate2  0  1) [0 (/ mount-height 2) 0]))
                        (if shift-down  (key-position column row (map - (wall-locate2  0 -1) [0 (/ mount-height 2) 0]))
                          (if shift-left (map + (left-key-position row 0) (wall-locate3 -1 0))
                            (key-position column row (map + (wall-locate2  1  0) [(/ mount-width 2) 0 0])))))]

    (->> shape (translate (map + offset [(first position) (second position) (+ (/ magnet-diameter 2) magnet-wall-width)])))))

;;;;;;;;;;;;;;;;;;;;;
;; CONTOLLER HOLES ;;
;;;;;;;;;;;;;;;;;;;;;
; Offsets for the controller/trrs holder cutout
(def holder-offset
  (case nrows
    0 0
    1 0
    2 0
    3 0
    4 -2.5
    5 0
    6 (if inner-column
        3.2
        2.2)
    )
  )

(def notch-offset
  (case nrows
    0 0
    1 0
    2 0
    3 0
    4 3.35
    5 0.15
    6 -5.07))

; Cutout for controller/trrs jack holder https://github.com/rianadon/dactyl-configurator/blob/main/src/connectors.md
(defn usb-holder-hole [width step height]
  (union
   (translate [0, (* -1 step), 0] (cube (- width (* step 2)), (* step  4), height ))
   (translate [0, step, 0] (cube width, step, height))
   (translate [0, (* -1 step), 0] (cube width, step, height))
   ))

(def left-offset -8)
(def usb-holder-hole-space
  (shape-insert 1, 0, [left-offset -0.5 (/ external-controller-height 2)]
                (usb-holder-hole external-controller-width external-controller-step external-controller-height)
                )
  )


;;;;;;;;;;;
;; SCREW ;;
;;;;;;;;;;;

(defn screw-insert-shape [bottom-radius top-radius height]
  (union
   (->> (binding [*fn* 30]
          (cylinder [bottom-radius top-radius] height)))
   (translate [0 0 (/ height 2)] (->> (binding [*fn* 30] (sphere top-radius))))))

(defn screw-insert [column row bottom-radius top-radius height offset]
  (let [shift-right   (= column lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (>= row lastrow))
        position      (if shift-up     (key-position column row (map + (wall-locate2  0  1) [0 (/ mount-height 2) 0]))
                        (if shift-down  (key-position column row (map - (wall-locate2  0 -1) [0 (/ mount-height 2) 0]))
                          (if shift-left (map + (left-key-position row 0) (wall-locate3 -1 0))
                            (key-position column row (map + (wall-locate2  1  0) [(/ mount-width 2) 0 0])))))]
    (->> (screw-insert-shape bottom-radius top-radius height)
         (translate (map + offset [(first position) (second position) (/ height 2)])))))

(defn screw-insert-all-shapes [bottom-radius top-radius height]
  (union
   (screw-insert 0 0         bottom-radius top-radius height [6 7 0])
   (screw-insert 0 lastrow   bottom-radius top-radius height [-2 1 0])
   (screw-insert lastcol lastrow  bottom-radius top-radius height [-4 15 0])
   (screw-insert lastcol 0         bottom-radius top-radius height [-4 7 0])

   (screw-insert 0 lastrow         bottom-radius top-radius height [15 -43 0])
   (screw-insert 2 lastrow         bottom-radius top-radius height [-4 -4 0])

   (screw-insert 3 0         bottom-radius top-radius height [-10 -3 0])
   )
  )

; Hole Depth Y: 4.4
(def screw-insert-height 4)

; Hole Diameter C: 4.1-4.4
(def screw-insert-bottom-radius (/ 4.4 2))
(def screw-insert-top-radius (/ 4.4 2))
(def screw-insert-holes  (screw-insert-all-shapes screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))

; Wall Thickness W:\t1.65
(def screw-insert-outers (screw-insert-all-shapes (+ screw-insert-bottom-radius 1.65) (+ screw-insert-top-radius 1.65) (+ screw-insert-height 1.5)))
(def screw-insert-screw-holes  (screw-insert-all-shapes 1.7 1.7 350))

(def pinky-connectors
  (apply union
         (concat
          ;; Row connections
          (for [row (range 0 lastrow)]
            (triangle-hulls
             (key-place lastcol row web-post-tr)
             (key-place lastcol row wide-post-tr)
             (key-place lastcol row web-post-br)
             (key-place lastcol row wide-post-br)))

          ;; Column connections
          (for [row (range 0 cornerrow)]
            (triangle-hulls
             (key-place lastcol row web-post-br)
             (key-place lastcol row wide-post-br)
             (key-place lastcol (inc row) web-post-tr)
             (key-place lastcol (inc row) wide-post-tr)))
          ;;
          )))

(def pinky-walls
  (union
   (key-wall-brace lastcol cornerrow 0 -1 web-post-br lastcol cornerrow 0 -1 wide-post-br)
   (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 0 1 wide-post-tr))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; wrist rest magnetic holder mounting ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn magnet-hole  [radius inner-radius height]
  (rotate [0 (deg2rad 90) (deg2rad 90)]
          (union
           (binding [*fn* 100] (cylinder radius height))
           (binding [*fn* 100] (cylinder inner-radius (+ height 10))
             )
           )
          )
  )

(defn magnet-stiffness-booster  [side height]
  (rotate [0 (deg2rad 90) (deg2rad 90)]
          (cube side side height)
          )
  )
; keyboard's magnet hole
(def magnet-place (union
                   (magnet-shape-insert 4, 2, [0 -13.5 0] (magnet-hole (+ (/ magnet-diameter 2) 0.1) (/ magnet-inner-diameter 2) magnet-height))
                   (magnet-shape-insert 3, 3, [0 -0.35 0] (magnet-hole (+ (/ magnet-diameter 2) 0.1) (/ magnet-inner-diameter 2) magnet-height))
                   )
  )

(def magnet-stiffness-booster (union
                               (magnet-shape-insert 4, 2, [0 (+ -13.5 wall-thickness) 0]
                                                   (magnet-stiffness-booster (+ magnet-diameter 2) magnet-booster-width)
                                                   )
                               (magnet-shape-insert 3, 3, [0 (+ -0.35 wall-thickness) 0]
                                                   (magnet-stiffness-booster (+ magnet-diameter 2) magnet-booster-width)
                                                   )
                               )
  )

; wrist rest magnet holder
(def magnet-connector
  (difference
   (rotate [0 (deg2rad 90) (deg2rad 90)]
           (translate [0 0 (/ magnet-connector-length -2)]
     (binding [*fn* 100] (cylinder (+ (/ magnet-diameter 2) magnet-connector-wall-width) magnet-connector-length))
           )
           )
     (magnet-hole (+ (/ magnet-diameter 2) 0.1) (/ magnet-inner-diameter 2) magnet-height)
   )
)

(def magnet-connector-offset 1)
(def magnet-connectors
  (union
   (magnet-shape-insert 4, 2, [0 (- -13 (+ magnet-connector-offset (/ magnet-height 2))) 0] magnet-connector)
   (magnet-shape-insert 3, 3, [0 (- 0.75 (+ magnet-connector-offset (/ magnet-height 2))) 0] magnet-connector)
   )
  )

(def model-right (difference
                  (union
                   key-holes-right
                   pinky-connectors
                   pinky-walls
                   connectors
                   thumb-right
                   thumb-connectors
                   (difference (union case-walls
                                      (if magnet-holes magnet-stiffness-booster)
                                      screw-insert-outers)
                               usb-holder-hole-space
                               screw-insert-holes
                               (if magnet-holes magnet-place)
                               ))
                  (translate [0 0 -20] (cube 350 350 40))))

(def model-left-tmp (difference
                  (union
                   key-holes-left
                   pinky-connectors
                   pinky-walls
                   connectors
                   thumb-left
                   thumb-connectors
                   (difference (union case-walls
                                      (if magnet-holes magnet-stiffness-booster)
                                      screw-insert-outers)
                               usb-holder-hole-space
                               screw-insert-holes
                               (if magnet-holes magnet-place)
                               ))
                  (translate [0 0 -20] (cube 350 350 40))))

(spit "things/right.scad"
      (write-scad model-right))

(spit "things/left.scad"
      (write-scad (mirror [-1 0 0] model-left-tmp)))

(spit "things/right-test.scad"
      (write-scad
       (difference
        (union
         key-holes-right
         pinky-connectors
         pinky-walls
         connectors
         thumb-right
         thumb-connectors
         (if magnet-holes magnet-connectors)
         case-walls
         thumbcaps
         caps)

        (translate [0 0 -20] (cube 350 350 40)))))

(if magnet-holes
  (spit "things/right-wrist-connector.scad"
      (write-scad
       (difference
        (union
         magnet-connectors
        )
        )
       )
        )

  (spit "things/left-wrist-connector.scad"
        (write-scad
         (mirror [-1 0 0]
                 (difference
          (union
           magnet-connectors
           )
                  )
                 )
         )
        )

)
;;;;;;;;;;;;;;;;;;;;;
;; plate generation;;
;;;;;;;;;;;;;;;;;;;;;

(def filled-plate
  (->> (cube mount-height mount-width plate-thickness)
       (translate [0 0 (/ plate-thickness 2)])
       ))

(defn key-places [shape]
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [2 3] column)
                         (not= row lastrow))]
           (->> shape
                (key-place column row)))))

(def key-fills
  (key-places filled-plate))

(defn thumb-layout [shape]
  (union
   (thumb-tr-place shape)
   (thumb-tl-place shape)
   (thumb-mr-place shape)
   (thumb-6-place shape)
   (thumb-br-place shape)
   (thumb-bl-place shape)
   ))

(def thumb-fill (thumb-layout filled-plate))

(def model-outline
  (project
   (union
    key-fills
    connectors
    thumb-fill
    thumb-right
    thumb-connectors
    case-walls
    )
   )
  )
(def case-walls-outline
  (project case-walls)
  )

(def wall-shape
  (cut
   (translate [0 0 -0.1]
              (union case-walls
                     screw-insert-outers
                     )
              ))
  )

(def bottom-height-half (/ plate-height 2))
(def bottom-plate
  (union
    (translate [0 0 bottom-height-half]
               (extrude-linear {:height plate-height :twist 0 :convexity 0} model-outline)
               )
   ( if (> plate-border-height 0)
     (translate [0 0 (+ plate-height (/ plate-border-height 2))]
                (extrude-linear {:height plate-border-height :twist 0 :convexity 0} case-walls-outline)
                )
       )
   )
  )
(def plate-right (difference
                  bottom-plate
                  screw-insert-screw-holes
                  ))

(spit "things/right-plate-print.scad" (write-scad plate-right))
(spit "things/left-plate-print.scad" (write-scad (mirror [-1 0 0] plate-right)))

(spit "things/hotswap-low-debug.scad" (write-scad hot-socket-low-profile))
(spit "things/hotswap-standart.scad" (write-scad hot-socket-standart))

(defn -main [dum] 1)  ; dummy to make it easier to batch
